# Supabase integration

The Android app is designed to use the `voice-rooms` Supabase project (`fdvgpdfesvigtvmzjhnt`) as its shared backend with the admin dashboard.

## Shared control model

The dashboard and app should use the same Supabase project and these server-controlled resources:

- `profiles` — users, coins, VIP, IDs, frames, bans, agency flags
- `rooms` — rooms and active/closed state
- `room_seats` — seat/mic state
- `room_messages` — room chat
- `gifts`, `room_gifts` — gifts and transactions
- `recharge_packages`, `recharge_orders`, `wallet_transactions` — recharge/wallet controls
- `vip_subscriptions` — VIP state
- `app_settings`, `app_config` — feature flags and remote configuration
- `ui_theme`, `ui_assets` — app branding, theme, 3D/visual assets and visibility/order
- `notifications` — server-controlled announcements

## Android environment

Do not commit Supabase secrets. The Android client must use only the public Supabase URL and publishable/anon key through the app's build configuration. Privileged service-role/database connection strings must remain server-side (dashboard/edge functions) and must never be shipped in the APK.

## Current backend

Supabase project ref: `fdvgpdfesvigtvmzjhnt`
Project name: `voice-rooms`

The GitHub Actions Android workflow builds and verifies a debug APK from `main`.
