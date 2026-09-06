# BTS Android — Architecture Freeze v1

Status: FROZEN for BTS Android v1

## BDR execution order
ARCHITECTURE → FREEZE → IMPLEMENT → TEST → DEPLOY

## Product source of truth
1. BTS Complete Project Manual v2.
2. Existing BTS Lovable website for visual and behavioral parity.
3. Existing Lovable Cloud / Supabase backend as the shared production data source.
4. BABA Development Rulebook principles agreed for this project.

## Native Android stack
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines + Flow
- ViewModel + immutable UI state
- Supabase Kotlin client for Auth/PostgREST/Realtime when backend runtime credentials are configured
- DataStore for local preferences and persisted cart/session-safe client state where appropriate
- Firebase Cloud Messaging for closed-app notifications in the notification milestone
- Razorpay Android SDK in the payment milestone

## Project structure
For v1 the project intentionally uses a single Android application Gradle module with strict package boundaries. This minimizes build complexity while preserving modular ownership.

- `core/` shared design, common utilities and platform abstractions
- `data/` backend DTOs, repositories and data sources
- `domain/` business models and use cases where logic is non-trivial
- `feature/` feature-owned screens, state and ViewModels
- `navigation/` app routes and navigation graph

No feature may bypass repositories to perform production backend access directly from UI code.

## Backend rule
The Android app and website use one backend and one dataset. Do not create a second production database and do not synchronize duplicate databases.

Never commit service-role keys, database passwords, signing keys, `.env` files, Razorpay secrets, Firebase service-account files or other secrets. Client-safe runtime values are supplied through local/CI configuration.

## UI parity
The manual's brand tokens, typography, spacing, components, mobile screenshots and behavior are authoritative. Native Compose equivalents must preserve these rather than redesigning the app.

## Frozen milestone sequence
M0 Foundation + repository + architecture
M1 Design system + shell + navigation
M2 Backend runtime connection + auth foundation
M3 Home + menu discovery
M4 Meal details + universal add-ons
M5 Cart + persisted configuration lines
M6 Build Meal engine
M7 Subscription lifecycle
M8 Orders + order state timeline
M9 Checkout + payments + wallet
M10 Profile + addresses + nutrition tracker + support
M11 Notifications + location + referrals
M12 Role-gated kitchen/packing/inventory/delivery surfaces
M13 Hardening: accessibility, performance, security, failure states
M14 Real-device acceptance test + APK/AAB release

## Change control
No architecture changes or feature additions during a milestone. New ideas go to `docs/BACKLOG.md` unless the user explicitly revises the frozen architecture.
