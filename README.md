# Pharaoh Party — Production Voice Rooms

This branch is the clean production implementation for the new Pharaoh Party Android voice-room application.

## Reference UI
The uploaded reference recording is used as the visual/interaction target: purple/gold premium theme, hot-room home, store, VIP/level presentation, 9-seat voice rooms, chat/gifts controls, profile and ID presentation.

## Backend
- Supabase project: voice-rooms
- Postgres + RLS + Realtime
- Username/password authentication through the `pharaoh-auth` Edge Function.
- One-device quick login through `quick_login_devices`.
- Atomic room seat claiming.
- Room bans, room staff, moderation reports and admin audit log.
- Wallet, gifts, store, inventory, VIP and agency tables/functions.
- Dashboard-controlled app configuration, feature visibility, UI assets and UI components.
- Production LiveKit token endpoint: `livekit-token`.

## Roles
- USER
- host / co-host / moderator
- host agency owner/member
- charge agent / recharge agency
- app administrator

## Core operations
- Create/join/leave/close rooms.
- Seat lock/mute/moderation.
- Realtime room messages and presence.
- Gift transactions with database-side balance checks.
- Recharge packages and wallet ledger.
- VIP subscriptions and levels.
- Follow/block/direct messaging/notifications.
- Agencies, agency balances, transfers and asset grants.
- Admin role assignment, wallet adjustments, bans, agency management and audit history.

## Security model
Client apps use only the Supabase publishable key. Privileged operations are database functions or Edge Functions. RLS is enabled on the application tables. Profile privilege fields such as coins, diamonds, VIP, admin and ban flags are protected by a database trigger.

## Voice
LiveKit API secrets must remain server-side. The Android app requests a short-lived room token from the `livekit-token` Edge Function. Configure `LIVEKIT_URL`, `LIVEKIT_API_KEY`, and `LIVEKIT_API_SECRET` as Supabase Edge Function secrets before real voice connections are enabled.

## Build
GitHub Actions workflow: `.github/workflows/android-pharaoh-party.yml`

The APK is considered release-ready only after the Actions build completes successfully and a physical-device smoke test confirms authentication, room join, microphone, realtime messages, gifts and store operations.