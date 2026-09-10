import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { authenticatedUser, constantTimeEqual, corsHeaders, hmacHex, json, razorpayAuthorization } from "../_shared/payment.ts";

type VerificationRequest = {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "Method not allowed" }, 405);

  try {
    const { user, admin } = await authenticatedUser(req);
    const body = await req.json() as VerificationRequest;
    if (!body.razorpayOrderId || !body.razorpayPaymentId || !/^[a-f0-9]{64}$/i.test(body.razorpaySignature ?? "")) {
      return json({ error: "Invalid verification payload" }, 400);
    }
    const { data: payment, error: paymentError } = await admin.from("payments")
      .select("id,order_id,amount,status")
      .eq("user_id", user.id)
      .eq("provider", "razorpay")
      .eq("provider_reference", body.razorpayOrderId)
      .single();
    if (paymentError || !payment) return json({ error: "Payment record not found" }, 404);
    if (payment.status === "SUCCESS") return json({ verified: true, orderId: payment.order_id, idempotent: true });

    const expected = await hmacHex(`${body.razorpayOrderId}|${body.razorpayPaymentId}`);
    if (!constantTimeEqual(expected.toLowerCase(), body.razorpaySignature.toLowerCase())) {
      return json({ error: "Payment signature is invalid" }, 400);
    }

    const providerResponse = await fetch(`https://api.razorpay.com/v1/payments/${encodeURIComponent(body.razorpayPaymentId)}`, {
      headers: { Authorization: razorpayAuthorization() },
    });
    let providerPayment = await providerResponse.json();
    const expectedAmount = Math.round(Number(payment.amount) * 100);
    if (!providerResponse.ok || providerPayment.order_id !== body.razorpayOrderId || providerPayment.amount !== expectedAmount || providerPayment.currency !== "INR" || !["authorized", "captured"].includes(providerPayment.status)) {
      return json({ error: "Payment provider confirmation failed" }, 409);
    }
    if (providerPayment.status === "authorized") {
      const captureResponse = await fetch(`https://api.razorpay.com/v1/payments/${encodeURIComponent(body.razorpayPaymentId)}/capture`, {
        method: "POST",
        headers: { Authorization: razorpayAuthorization(), "Content-Type": "application/json" },
        body: JSON.stringify({ amount: expectedAmount, currency: "INR" }),
      });
      providerPayment = await captureResponse.json();
      if (!captureResponse.ok || providerPayment.status !== "captured") return json({ error: "Payment capture failed" }, 409);
    }

    const { error: orderError } = await admin.from("orders").update({ status: "PAID" })
      .eq("id", payment.order_id).eq("user_id", user.id);
    if (orderError) throw orderError;
    const { error: updateError } = await admin.from("payments").update({
      status: "SUCCESS",
      reference_note: `razorpay_payment_id:${body.razorpayPaymentId}`,
    }).eq("id", payment.id).eq("user_id", user.id).eq("status", "PENDING");
    if (updateError) throw updateError;

    return json({ verified: true, orderId: payment.order_id });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Verification failed";
    const status = message.includes("Authentication") || message.includes("session") ? 401 : 500;
    return json({ error: status === 500 ? "Payment could not be verified" : message }, status);
  }
});
