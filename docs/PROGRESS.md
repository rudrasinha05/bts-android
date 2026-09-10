# BTS Android Progress

Last updated: 2026-09-09

## BDR status
- Architecture: FROZEN
- Active branch: `develop`
- Stable branch: `main`
- Backend rule: reuse the existing BTS production backend; no new production database

## Milestones
- M0 Foundation + repository + architecture — COMPLETE
- M1 Design system + shell + navigation — COMPLETE (emulator validation confirmed)
- M2 Backend runtime connection + auth foundation — COMPLETE (CI + emulator OAuth validation confirmed)
- M3 Home + menu discovery — COMPLETE (CI + emulator validation confirmed)
- M4 Meal details + universal add-ons — COMPLETE (CI + emulator validation confirmed)
- M5 Cart + persisted configuration lines — COMPLETE (CI + emulator validation confirmed)
- M6 Build Meal engine — COMPLETE (CI + emulator validation confirmed)
- M7 Subscription lifecycle — COMPLETE (CI + emulator validation confirmed)
- M8 Orders + order state timeline — COMPLETE (CI + emulator validation confirmed)
- M9 Checkout + payments + wallet — COMPLETE (CI + emulator payment validation confirmed)
- M10 Profile + addresses + nutrition tracker + support — COMPLETE (CI + emulator validation confirmed)
- M11 Notifications + location + referrals — IN PROGRESS (FCM integration added; deployment validation pending)
- M12 Role-gated operations surfaces — NOT STARTED
- M13 Hardening — NOT STARTED
- M14 Acceptance testing + APK/AAB — NOT STARTED

## M1 delivered
- BTS light/dark color tokens
- Typography contract
- Spacing/radius/shell dimensions
- Persistent top bar
- Runtime light/dark theme toggle
- Persistent five-item mobile bottom navigation
- Adaptive left navigation rail for tablet/wide layouts
- Hamburger navigation drawer
- Notification and cart entry points in top bar
- Route shell for Home, Menu, Plans, Dashboard, Build Meal, Subscription, Orders, Nutrition, Profile, Support and Cart

## M1 exit criteria
1. CI green on `develop`.
2. App launches on emulator/device.
3. Bottom navigation switches routes without duplicate stack growth.
4. Drawer opens/closes and navigates.
5. Cart top-bar action navigates to Cart.
6. Light/dark theme renders without crash.
7. Wide/tablet layout switches to the navigation rail.

Do not start M2 until these M1 exit criteria are satisfied.

## M2 implementation started
- Supabase Kotlin client is pinned and isolated under `data/`.
- Runtime URL and publishable key come from local/CI configuration; secrets are not committed.
- Auth repository supports email/password sign-in and sign-up, Indian mobile OTP, Google OAuth and sign-out.
- Session state is exposed to Compose through an auth ViewModel.
- Android deep-link callback is `bts://auth`.
- No schema, migration, table, RLS policy or production data change is part of M2.
- Authenticated sessions load application roles from the existing `user_roles` table through RLS.
- Protected customer routes redirect guests to Auth and resume the requested destination after sign-in.

## M2 live backend validation
- Authoritative BTS project: `fgrhxihpvuxjmkbqvlik`.
- Project URL and client-safe publishable key validated against the live Data API.
- Existing `meals` endpoint returns HTTP 200.
- Existing `user_roles` endpoint returns HTTP 200 and an empty anonymous result, consistent with RLS protection.
- Email auth is enabled and the Google provider has been configured for the BTS Android OAuth callback.
- Google sign-in emulator round-trip validated with a persisted authenticated customer session.
- Phone OTP remains provider-dependent and is not an M2 completion blocker.
- Client-safe credentials remain local/CI configuration and are not committed.

## M3 implementation started
- Available, active, non-add-on meals load from the existing `meals` table through a repository.
- Home discovery surfaces backend-driven popular meals with loading, retry and failure states.
- Menu discovery provides backend-derived meal categories and food-type filters.
- No schema, table, migration, RLS policy or production data change is part of M3.

## M4 implementation started
- Menu and Home meal cards open a native meal-detail route.
- Details render existing meal metadata, tags, portion and allergen warnings.
- Universal add-ons load from active, available `meals` rows marked `is_addon=true`.
- Add-on quantity controls calculate a configured total without crossing into M5 cart persistence.
- No schema, table, migration, RLS policy or production data change is part of M4.

## M5 implementation started
- Configured meal and add-on selections persist locally through Preferences DataStore.
- Distinct configuration lines preserve meal, add-ons, quantities and calculated totals.
- Cart supports quantity changes, line removal, clearing and subtotal calculation.
- Checkout and backend order creation remain isolated to their frozen milestones.
- No schema, table, migration, RLS policy or production data change is part of M5.

## M6 implementation started
- Four-step controlled builder covers meal time, food preference, one main meal and universal add-ons.
- Choices are constrained to active, available rows already loaded from the shared `meals` table.
- Builder calculates the configured total and hands the exact configuration to the persisted M5 cart.
- Selection guards prevent incomplete meals and cap add-on quantities.
- No schema, table, migration, RLS policy or production data change is part of M6.

## M7 implementation started
- Active plans load from the existing `subscription_plans` table.
- Authenticated customer subscriptions load through RLS from `subscriptions`.
- Existing subscriptions expose pause, resume and cancel actions with user-id scoped updates.
- New paid subscription activation remains isolated to M9 checkout/payments.
- No schema, table, migration, RLS policy or production data change is part of M7.

## M8 implementation started
- Authenticated customer orders load from the existing RLS-protected `orders` table.
- Order details combine existing `order_items` and `order_status_history` records.
- List and detail screens expose totals, item quantities, notes and chronological status history.
- Empty accounts render an intentional no-orders state; order creation remains in M9 checkout.
- No schema, table, migration, RLS policy or production data change is part of M8.

## M9 implementation started
- Cart now hands authenticated customers into a native checkout review screen.
- Checkout reads active coupons and the authenticated customer's wallet balance from the existing backend.
- Coupon discounts and wallet/online payment choices update an explicit payable summary.
- Payment execution stays locked until the existing server-side checkout contract is identified; no Razorpay secret or payment verification is placed in Android.
- Version-controlled `create-payment-order` and `verify-payment` Edge Functions now implement authenticated server pricing, coupon checks, Razorpay order creation, signature verification and capture against existing tables.
- Edge Function deployment and Android Razorpay callback wiring remain pending live validation.
- Wallet checkout uses an authenticated server function with a compare-and-set balance debit, payment record and wallet ledger entry against the existing tables.
- No schema, table, migration, RLS policy or production data change is part of M9.

## M10 implementation started
- Authenticated customers can edit existing profile fields and manage active/default delivery addresses.
- Nutrition goals persist through `nutrition_profiles`; meal nutrition reads the existing `meal_nutrition` records.
- Support tickets can be created and reviewed through the existing `support_tickets` table.
- No schema, table, migration, RLS policy or production data change is part of M10.

## M11 implementation started
- The top-bar bell opens the existing RLS-scoped notification inbox and supports read state.
- Referral code sharing and reward history use the existing referral tables.
- Customers can attach device coordinates to an owned saved address after runtime location permission.
- Added the required `push_device_tokens` migration with authenticated ownership RLS for closed-app FCM registration.
- Added Firebase Messaging service and signed-in token registration. `google-services.json` remains local/untracked.
