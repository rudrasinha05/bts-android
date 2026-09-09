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
- M3 Home + menu discovery — IN PROGRESS
- M4 Meal details + universal add-ons — NOT STARTED
- M5 Cart + persisted configuration lines — NOT STARTED
- M6 Build Meal engine — NOT STARTED
- M7 Subscription lifecycle — NOT STARTED
- M8 Orders + order state timeline — NOT STARTED
- M9 Checkout + payments + wallet — NOT STARTED
- M10 Profile + addresses + nutrition tracker + support — NOT STARTED
- M11 Notifications + location + referrals — NOT STARTED
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
