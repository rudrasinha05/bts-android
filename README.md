# BTS — Baba Tiffin Services Android

Native Android application for Baba Tiffin Services.

## Source of truth
- BTS Complete Project Manual v2
- Existing BTS Lovable web platform and backend
- BABA Development Rulebook (BDR)

## Development rule
ARCHITECTURE → FREEZE → IMPLEMENT → TEST → DEPLOY

No Flutter. No FlutterFlow. Native Android only: Kotlin + Jetpack Compose + Android Studio.

The Android app must use the same BTS backend and business data as the website. No duplicate production database.

## Local backend configuration
Add the existing BTS project's client-safe values to the untracked `local.properties` file:

```properties
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_PUBLISHABLE_KEY=YOUR_CLIENT_SAFE_PUBLISHABLE_KEY
```

Never use a service-role/secret key in the Android app. Configure `bts://auth` as an allowed
Supabase Auth redirect URL for Google OAuth and authentication links.
