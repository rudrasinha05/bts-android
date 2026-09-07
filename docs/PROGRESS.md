# BTS Android Progress

Last updated: 2026-09-07

## BDR status
- Architecture: FROZEN
- Active branch: `develop`
- Stable branch: `main`
- Backend rule: reuse the existing BTS production backend; no new production database

## Milestones
- M0 Foundation + repository + architecture — COMPLETE
- M1 Design system + shell + navigation — CODE COMPLETE, VALIDATION PENDING
- M2 Backend runtime connection + auth foundation — NOT STARTED
- M3 Home + menu discovery — NOT STARTED
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
