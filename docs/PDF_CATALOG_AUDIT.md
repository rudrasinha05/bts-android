# BTS PDF Catalogue Audit

Audit date: 2026-09-13

Source: `BTS-Complete-Project-Manual-v2`, pages 24-25 and 29-30, compared with the production Supabase project.

| Area | PDF target | Production result | Status |
| --- | ---: | ---: | --- |
| Meals | 116 | 62 (53 base meals + 9 add-ons) | 54 records missing from the live catalogue |
| Menu slots | 24 total / 21 active | 24 total / 21 active | Complete |
| Menu-slot options | 337 on page 24; 334 later in the technical section | 240 | Incomplete; PDF itself has a 3-row inconsistency |
| Add-on categories | 8 | 8 active | Complete |
| Subscription plans | 10 | 10 active | Complete; names, cycles, meal counts, food types and prices match |
| Dish photos | 71 bundled assets | All 71 PDF photos are bundled in an Android atlas and mapped by canonical meal name/aliases; all 62 backend `image_url` values remain null | Implemented for every matchable PDF meal; unknown names use a safe fallback |
| Published dated menus | Required | 21 schedules / 154 available menu items | Data exists, but Android discovery currently reads the meal catalogue rather than dated menu rows |

## Safe remediation rule

Do not fabricate the missing 54 meal definitions or 94-97 menu-slot options. Their exact names, prices, nutrition, eligibility and slot bindings are not present in the PDF. Import them only from the original website seed/migration/export so the shared production dataset remains authoritative.

## Implemented interaction remediation

- Home and Menu meal cards expose an `Add` button.
- Tapping `Add` immediately persists the base meal in the cart.
- The universal add-ons dialog opens immediately.
- Main-meal and add-on quantity steppers update the same persisted cart line.
- Home renders horizontal Popular, Breakfast, Lunch, Dinner, Chicken, Egg, Fish and Special rails when each category has data.
- Every Home and Menu meal card renders the mapped PDF photo and an `Add` button.

## Location and checkout safeguards

- App startup checks location permission while keeping Home as the guest start destination.
- Signed-in customers automatically attach the newest available device location to their default/first saved address.
- Checkout remains login-protected and requires at least one saved delivery address.
- The customer must explicitly confirm the selected delivery location before payment is enabled.
