import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { authenticatedUser, corsHeaders, json } from "../_shared/payment.ts";

type Item = { mealId: string; quantity: number };
type RequestBody = { items: Item[]; couponCode?: string; mealType: "breakfast" | "lunch" | "dinner"; addressId?: string; notes?: string };

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const { user, admin } = await authenticatedUser(req);
    const body = await req.json() as RequestBody;
    if (!Array.isArray(body.items) || body.items.length === 0 || body.items.length > 50) return json({ error: "Invalid cart" }, 400);
    if (!["breakfast", "lunch", "dinner"].includes(body.mealType)) return json({ error: "Invalid meal type" }, 400);
    if (body.addressId) {
      const { data: address } = await admin.from("addresses").select("id").eq("id", body.addressId).eq("user_id", user.id).maybeSingle();
      if (!address) return json({ error: "Delivery address was not found" }, 400);
    }

    const quantities = new Map<string, number>();
    for (const item of body.items) {
      if (!item.mealId || !Number.isInteger(item.quantity) || item.quantity < 1 || item.quantity > 20) return json({ error: "Invalid cart item" }, 400);
      quantities.set(item.mealId, (quantities.get(item.mealId) ?? 0) + item.quantity);
    }
    const ids = [...quantities.keys()];
    const { data: meals, error: mealsError } = await admin.from("meals").select("id,name,price")
      .in("id", ids).eq("is_active", true).eq("is_available", true);
    if (mealsError) throw mealsError;
    if (!meals || meals.length !== ids.length) return json({ error: "One or more meals are unavailable" }, 409);

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
      discountPaise = Math.min(subtotalPaise, Math.max(
        Math.round(Number(coupon.discount_amount ?? 0) * 100),
        Math.round(subtotalPaise * Number(coupon.discount_percent ?? 0) / 100),
      ));
      couponId = coupon.id;
    }
    const total = (subtotalPaise - discountPaise) / 100;
    const { data: wallet } = await admin.from("wallets").select("id,balance").eq("user_id", user.id).maybeSingle();
    if (!wallet || Number(wallet.balance) < total) return json({ error: "Insufficient wallet balance" }, 409);
    const oldBalance = Number(wallet.balance);
    const newBalance = Number((oldBalance - total).toFixed(2));
    const { data: debited, error: debitError } = await admin.from("wallets").update({ balance: newBalance })
      .eq("id", wallet.id).eq("user_id", user.id).eq("balance", oldBalance).select("id").maybeSingle();
    if (debitError) throw debitError;
    if (!debited) return json({ error: "Wallet balance changed; please retry" }, 409);

    let orderId: string | null = null;
    try {
      const { data: order, error: orderError } = await admin.from("orders").insert({
        user_id: user.id, address_id: body.addressId ?? null, meal_type: body.mealType, status: "PAID",
        subtotal: subtotalPaise / 100, discount: discountPaise / 100, total, notes: body.notes?.slice(0, 500) ?? null,
      }).select("id,order_number").single();
      if (orderError) throw orderError;
      orderId = order.id;
      const { error: itemError } = await admin.from("order_items").insert(meals.map((meal) => ({
        order_id: order.id, meal_id: meal.id, meal_name: meal.name,
        quantity: quantities.get(meal.id), unit_price: Number(meal.price),
      })));
      if (itemError) throw itemError;
      const reference = `wallet_${crypto.randomUUID()}`;
      const { error: paymentError } = await admin.from("payments").insert({
        user_id: user.id, order_id: order.id, amount: total, currency: "INR", status: "SUCCESS",
        provider: "bts_wallet", provider_reference: reference, coupon_id: couponId, method: "WALLET",
      });
      if (paymentError) throw paymentError;
      const { error: ledgerError } = await admin.from("wallet_transactions").insert({
        user_id: user.id, amount: total, direction: "debit", reason: "order_payment",
        order_id: order.id, balance_after: newBalance,
      });
      if (ledgerError) throw ledgerError;
      return json({ verified: true, orderId: order.id, orderNumber: order.order_number, balance: newBalance });
    } catch (error) {
      if (orderId) {
        await admin.from("payments").delete().eq("order_id", orderId).eq("user_id", user.id);
        await admin.from("order_items").delete().eq("order_id", orderId);
        await admin.from("orders").delete().eq("id", orderId).eq("user_id", user.id);
      }
      await admin.from("wallets").update({ balance: oldBalance }).eq("id", wallet.id).eq("user_id", user.id).eq("balance", newBalance);
      throw error;
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : "Wallet payment failed";
    const status = message.includes("Authentication") || message.includes("session") ? 401 : 500;
    return json({ error: status === 500 ? "Wallet payment could not be completed" : message }, status);
  }
});
