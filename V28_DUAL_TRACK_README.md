# Montrix V28 — dual-track architecture integration

**Not a completed feature-parity refactor.** This package preserves the uploaded, emulator-tested `DashboardActivity` and all baseline source files, while adding the refactored dashboard as a separately launchable activity. It is a safe integration/testing checkpoint, not a production replacement.

## Why this arrangement
The review refactor does not connect radio, RTL-SDR, offline map, CAN telemetry, App List, Bluetooth, shift tracking, or photo selection. Replacing the original Activity would break these features. The existing Activity is retained byte-for-byte. No original source files were deleted.

## Test candidate
Build `:app:assembleDebug`, install on `emulator-5554`, then run:

`adb -s emulator-5554 shell am start -n com.monte.montrix/.RefactorDashboardActivity`

Launch the existing working dashboard with:

`adb -s emulator-5554 shell am start -n com.monte.montrix/.DashboardActivity`

## Release gate
Do not make RefactorDashboardActivity the default launcher until every feature is wired, all architecture checks pass, Android compilation succeeds, and emulator tests pass. The production Activity currently violates the desired separation, deliberately retained for regression safety.
