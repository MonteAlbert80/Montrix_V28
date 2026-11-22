# Montrix 27 — incremental modular refactor

## Status
This is a **development source release**, not a verified deployable APK. The existing 17 Java classes remain intact except for narrow integration changes. New code is isolated in packages.

## Implemented
- `branding/Brand.java`: central product identity.
- `radio/RadioTextPresenter.java`: first line long RDS RadioText, second line PS station name; frequency in source label; marquee setup.
- `trips/TripDatabase.java`: SQLite raw GPS, trips, stops, turns, shifts, trip-shift links and events tables.
- `trips/TripRecorder.java`: raw GPS persistence and provisional distance grouping while dashboard is running on physical unit.
- `trips/TripReviewRepository.java`: change trip classification without deleting raw GPS.
- Dashboard integrates recorder at onLocationChanged and RDS presenter at radio state receiver.

## NOT IMPLEMENTED / NOT CLAIMED
- Full background collection when dashboard is not running, ignition recovery, location notification, battery optimization strategy.
- Reliable work-shift inference, stop/turn detection, map matching, route instructions, trip-review screen, CSV/GPX export.
- Factory radio foreground supervision (do not force-stop tuner process blindly).
- Hardware-verified presets, audio sleep/wake, CAN, Texas speed-limit database.
- Full UI decomposition. DashboardActivity remains large and must be split incrementally with tests.
- Actual build verification; no Android SDK/Gradle installed in this execution environment.

## Architecture rule
Modify one module for one issue, preserving public interfaces. Never edit OEM radio service for visual text changes. Keep source control commits and regression checks for every change.

## Safety
The SQLite schema is versioned and throws on unknown migrations rather than destroying stored trip data. Simulator GPS is not persisted. Trip history is sensitive: stored locally, no upload, no cloud sync, no analytics.
