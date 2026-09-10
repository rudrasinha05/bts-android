import { createClient } from "npm:@supabase/supabase-js@2.57.4";

export const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
};

export const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: { ...corsHeaders, "Content-Type": "application/json" },
});

export function env(name: string): string {
  const value = Deno.env.get(name);
  if (!value) throw new Error(`Missing server configuration: ${name}`);
  return value;
}

export function clients(req: Request) {
  const authorization = req.headers.get("Authorization") ?? "";
  if (!authorization.startsWith("Bearer ")) throw new Error("Authentication required");
  const url = env("SUPABASE_URL");
  const publishableKeys = JSON.parse(env("SUPABASE_PUBLISHABLE_KEYS"));
  const secretKeys = JSON.parse(env("SUPABASE_SECRET_KEYS"));
  return {
    userClient: createClient(url, publishableKeys.default, { global: { headers: { Authorization: authorization } } }),
    admin: createClient(url, secretKeys.default, { auth: { persistSession: false, autoRefreshToken: false } }),
  };
}

export async function authenticatedUser(req: Request) {
  const { userClient, admin } = clients(req);
  const { data, error } = await userClient.auth.getUser();
  if (error || !data.user) throw new Error("Invalid or expired session");
  return { user: data.user, admin };
}

export function razorpayAuthorization() {
  return `Basic ${btoa(`${env("RAZORPAY_KEY_ID")}:${env("RAZORPAY_KEY_SECRET")}`)}`;
}

export async function hmacHex(value: string): Promise<string> {
  const encoder = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(env("RAZORPAY_KEY_SECRET")),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign("HMAC", key, encoder.encode(value));
  return Array.from(new Uint8Array(signature)).map((byte) => byte.toString(16).padStart(2, "0")).join("");
}

export function constantTimeEqual(left: string, right: string): boolean {
  if (left.length !== right.length) return false;
  let difference = 0;
  for (let index = 0; index < left.length; index++) difference |= left.charCodeAt(index) ^ right.charCodeAt(index);
  return difference === 0;
}
