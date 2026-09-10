import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { authenticatedUser, corsHeaders, json, razorpayAuthorization } from "../_shared/payment.ts";

type CartItem = { mealId: string; quantity: number };
type CheckoutRequest = {
  items: CartItem[];
  couponCode?: string;
  mealType: "breakfast" | "lunch" | "dinner";
  addressId?: string;
  notes?: string;
  method?: "UPI" | "QR" | "CARD";
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  try {
    const { user, admin } = await authenticatedUser(req);
    const body = await req.json() as CheckoutRequest;
    if (!Array.isArray(body.items) || body.items.length === 0 || body.items.length > 50) return json({ error: "Invalid cart" }, 400);
    if (!["breakfast", "lunch", "dinner"].includes(body.mealType)) return json({ error: "Invalid meal type" }, 400);
    if (body.method && !["UPI", "QR", "CARD"].includes(body.method)) return json({ error: "Invalid payment method" }, 400);
    if (body.addressId) {
      const { data: address } = await admin.from("addresses").select("id").eq("id", body.addressId).eq("user_id", user.id).maybeSingle();
      if (!address) return json({ error: "Delivery address was not found" }, 400);
    }

    const quantities = new Map<string, number>();
    for (const item of body.items) {
      if (!item.mealId || !Number.isInteger(item.quantity) || item.quantity < 1 || item.quantity > 20) return json({ error: "Invalid cart item" }, 400);
      quantities.set(item.mealId, (quantities.get(item.mealId) ?? 0) + item.quantity);
    }
    const mealIds = [...quantities.keys()];
    const { data: meals, error: mealsError } = await admin.from("meals")
      .select("id,name,price")
      .in("id", mealIds)
      .eq("is_active", true)
      .eq("is_available", true);
    if (mealsError) throw mealsError;
    if (!meals || meals.length !== mealIds.length) return json({ error: "One or more meals are unavailable" }, 409);

    const subtotalPaise = meals.reduce((sum, meal) => sum + Math.round(Number(meal.price) * 100) * (quantities.get(meal.id) ?? 0), 0);
    let discountPaise = 0;
    let couponId: string | null = null;
    if (body.couponCode) {
      const today = new Date().toISOString().slice(0, 10);
      const { data: coupon } = await admin.from("coupons").select("id,discount_percent,discount_amount,valid_from,valid_to,max_redemptions")
        .eq("code", body.couponCode.trim().toUpperCase()).eq("is_active", true).maybeSingle();
      if (!coupon || (coupon.valid_from && coupon.valid_from > today) || (coupon.valid_to && coupon.valid_to < today)) return json({ error: "Coupon is not valid" }, 400);
      if (coupon.max_redemptions != null) {
        const { count } = await admin.from("payments").select("id", { count: "exact", head: true })
          .eq("coupon_id", coupon.id).in("status", ["PENDING", "SUCCESS"]);
        if ((count ?? 0) >= coupon.max_redemptions) return json({ error: "Coupon redemption limit reached" }, 409);
      }
      const fixed = Math.round(Number(coupon.discount_amount ?? 0) * 100);
      const percent = Math.round(subtotalPaise * Number(coupon.discount_percent ?? 0) / 100);
      discountPaise = Math.min(subtotalPaise, Math.max(fixed, percent));
      couponId = coupon.id;
    }
    const totalPaise = subtotalPaise - discountPaise;
    if (totalPaise < 100) return json({ error: "Payable total must be at least ₹1" }, 400);

    const receipt = `bts_${crypto.randomUUID().replaceAll("-", "").slice(0, 24)}`;
    const razorpayResponse = await fetch("https://api.razorpay.com/v1/orders", {
      method: "POST",
      headers: { Authorization: razorpayAuthorization(), "Content-Type": "application/json" },
      body: JSON.stringify({ amount: totalPaise, currency: "INR", receipt, notes: { user_id: user.id } }),
    });
    const razorpayOrder = await razorpayResponse.json();
    if (!razorpayResponse.ok || !razorpayOrder.id) return json({ error: "Payment provider could not create an order" }, 502);

    const { data: order, error: orderError } = await admin.from("orders").insert({
      user_id: user.id,
      address_id: body.addressId ?? null,
      meal_type: body.mealType,
      status: "PAYMENT_PENDING",
      subtotal: subtotalPaise / 100,
      discount: discountPaise / 100,
      total: totalPaise / 100,
      notes: body.notes?.slice(0, 500) ?? null,
    }).select("id,order_number").single();
    if (orderError) throw orderError;

    try {
      const orderItems = meals.map((meal) => ({
        order_id: order.id,
        meal_id: meal.id,
        meal_name: meal.name,
        quantity: quantities.get(meal.id),
        unit_price: Number(meal.price),
      }));
      const { error: itemsError } = await admin.from("order_items").insert(orderItems);
      if (itemsError) throw itemsError;
      const { error: paymentError } = await admin.from("payments").insert({
        user_id: user.id,
        order_id: order.id,
        amount: totalPaise / 100,
        currency: "INR",
        status: "PENDING",
        provider: "razorpay",
        provider_reference: razorpayOrder.id,
        coupon_id: couponId,
        method: body.method ?? "UPI",
      });
      if (paymentError) throw paymentError;
    } catch (error) {
      await admin.from("order_items").delete().eq("order_id", order.id);
      await admin.from("orders").delete().eq("id", order.id).eq("user_id", user.id);
      throw error;
    }

    return json({
      orderId: order.id,
      orderNumber: order.order_number,
      razorpayOrderId: razorpayOrder.id,
      keyId: Deno.env.get("RAZORPAY_KEY_ID"),
      amount: totalPaise,
      currency: "INR",
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Checkout failed";
    const status = message.includes("Authentication") || message.includes("session") ? 401 : 500;
    return json({ error: status === 500 ? "Checkout could not be started" : message }, status);
  }
});
