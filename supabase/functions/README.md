# BTS payment Edge Functions

These functions are deployed to the existing BTS Supabase project. They create and verify Razorpay payments without exposing a Razorpay secret to Android.

Required project secrets:

- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`

Deploy `create-payment-order`, `verify-payment` and `pay-with-wallet` with JWT verification enabled. No database migration is included or required.
