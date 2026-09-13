# BTS Android M14 Release Checklist

## 1. Local signing setup

Create the release keystore outside Git, then add an ignored `keystore.properties` file in the project root:

```properties
storeFile=C:/secure/bts-release.jks
storePassword=REPLACE_LOCALLY
keyAlias=bts
keyPassword=REPLACE_LOCALLY
```

Never commit the keystore or its passwords.

## 2. Build gates

- `gradle :app:testDebugUnitTest`
- `gradle :app:lintDebug`
- `gradle :app:assembleRelease`
- `gradle :app:bundleRelease`

## 3. Real-device acceptance

- Fresh install launches without a crash.
- Email/password and Google sign-in complete successfully.
- Home/menu live data, meal configuration, cart persistence and checkout work.
- Razorpay test payment and wallet payment produce correct order/payment records.
- Profile, address, nutrition, support, notifications and referrals work.
- Notification arrives while app is foregrounded, backgrounded and closed.
- Customer cannot open operations routes; authorized roles can complete status transitions.
- Dark mode, top bar, drawer and bottom navigation remain stable across screens.
- Offline/error states show retry actions and recover after connectivity returns.
- Sign-out clears protected navigation and a subsequent sign-in reloads current roles.

## 4. Release cleanup

- Remove the temporary `admin` role from the test customer account.
- Confirm only publishable Supabase credentials are packaged in the client.
- Confirm Razorpay and Supabase server secrets exist only in Edge Function secrets.
- Archive the signed APK/AAB, mapping file and keystore backup securely.
