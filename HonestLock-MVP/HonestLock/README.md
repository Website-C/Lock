# Honest Lock — MVP

An Android app that locks Instagram, YouTube and Facebook behind a PIN + an
"honesty gate" quote, then tracks combined usage and escalates:

- **30 min** → pop-up quote
- **45 min** (+15) → warning
- **55 min** (+10) → last warning, 60s grace, then force-close
- Force-close → **all 3 apps locked for 15 min**
- **4 hours combined** in a day → locked out until midnight, regardless of stage

## Why Android, and why not a web app

This needs three OS-level permissions no browser or web app can get:
Accessibility (to detect and close other apps), draw-over-other-apps
(the pop-ups), and a foreground service (the timer). That only exists for a
real installed app. **iOS doesn't allow this at all** — third-party apps
can't overlay, monitor, or force-close other apps; Apple's own Screen Time
API is far more restricted and can't reproduce this exact flow.

I can't compile or run an Android build in this environment, so treat this
as a solid, logically-complete starting point, not a guaranteed one-tap
build — expect a couple of small fixes in Android Studio.

## Setup

1. In Android Studio: **New Project → Empty Views Activity**, package
   `com.honestlock.app`, language Kotlin, min SDK 26.
2. Delete the generated sample files, then copy everything from this
   project into the new one (same folder structure).
3. Sync Gradle (Studio will offer to fix the wrapper automatically).
4. Run on a **real device** — Accessibility/overlay flows are unreliable
   on the emulator.
5. Open the app, set a PIN, then tap through the three "Grant…" buttons
   in the dashboard (Accessibility, Overlay, Notifications).
6. On some phones (Samsung, Xiaomi, etc.) also disable battery
   optimization for Honest Lock and allow "autostart" — otherwise the
   OS may kill the timer service.

## Tuning

All thresholds are constants at the top of
`UsageMonitorService.kt` — change `STAGE1_MS`, `STAGE2_MS`, `STAGE3_MS`,
`FORCE_CLOSE_GRACE_MS`, `LOCKOUT_DURATION_MS`, `DAILY_CAP_MS`.
Quotes live in `Quotes.kt`. Monitored package names are in
`MonitoredApps.kt` — add `com.facebook.lite` etc. if you use a variant.

## Assumptions made (change if wrong)

- The 30/45/55-minute clock is **combined time across all three apps**,
  and resets each time you leave the category (go to home/another app);
  only the 4-hour total is a hard daily cumulative cap.
- 60 seconds of grace after the last warning before it force-closes.
- Daily-cap lockout lasts until midnight, local time; escalation lockout
  is a flat 15 minutes.
- The honesty gate is per "sitting" across the category, not per
  individual app — confirm once, move freely between the three until you
  leave or get locked out.

## Before publishing (not needed for personal use)

Sideloading this on your own phone is fine as-is. If you ever want it on
the Play Store, Google reviews Accessibility API usage closely — you'd
need to go through their permitted-use declaration for the app-blocking
category before it'd be approved.
