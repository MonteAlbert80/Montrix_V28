# Montrix V28 — review status (2026-11-22)

## Identity verification
- `applicationId`: `com.monte.montrix`
- `namespace`: `com.monte.montrix`
- `versionCode`: `280000`
- `versionName`: `28.0.0-dev`
- `Brand.RELEASE`: `28.0.0-dev`
- Manifest launcher activity: `com.monte.montrix.DashboardActivity`
- The manifest does not declare a V27 version.

## Implemented in this review increment
- Nine additional independently named Model/View pairs: Radio, Music, Bluetooth, SpeedLimit, Temperature, Map, Shift, VehicleStatus, Clock.
- Speed-limit and temperature formatting moved behind corresponding service snapshots in two existing rendering paths.
- Preserved existing V28 CurrentSpeed, AppList and PictureAlbum source.
- Kept legacy `com.monte.cwgradio.*` broadcast action strings as explicit compatibility identifiers; these are NOT the application ID and changing them without updating external emitters would break interoperability.

## NOT COMPLETE / NOT VERIFIED
- DashboardActivity remains a monolith and still creates dashboard layout and manages music, radio, map, GPS, and simulator. The requested full architectural extraction has NOT been accomplished.
- Most newly created Model/View pairs are scaffolding and not wired into DashboardActivity.
- Actual physical CAN speed decoding is not verified.
- Windows RTL-SDR pipeline is not end-to-end verified.
- No Android SDK/Gradle build environment is available here; no APK build or instrumented test was run.
- Comprehensive Javadoc for the legacy Java source is still outstanding.
- Do not deploy this review checkpoint to the car.

## Next required work
1. Move widget creation/composition to DashboardView and individual component Views.
2. Move media state/control and RDS into service layer.
3. Move location and speed-limit data handling to services.
4. Move shift state and trip recording to services.
5. Move emulator broadcast handling to providers, not Activity.
6. Replace placeholder components with fully wired, testable implementations.
7. Compile Android APK and run regression tests against both FYT and Windows SDR emulator.
