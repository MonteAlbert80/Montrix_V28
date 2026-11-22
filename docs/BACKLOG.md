# Montrix comprehensive backlog

## P0: Physical head-unit correctness
- Factory radio OEM UI auto-launch after ACC/wake: detect foreground and return to Montrix without killing tuner/audio backend.
- Preset tune dispatch/confirmation, seek, selected state, station labels, long-press save.
- RDS PS versus RT identity, changing metadata, stale RDS after station change, long scrolling, seek-button width.
- Music silent playback advancement while ignition off; pause/resume on ACC transitions; retain seamless cranking.
- Distinguish real ACC, screen-off, power disconnect, hibernation, simulator ACC.
- Confirm real hardware audio focus and factory tuner process behavior.

## P1: Trips, shifts and location
- Continuous GPS recorder independent of manual shift buttons.
- Foreground location service, restart recovery, permission and storage limits.
- Detect trip start/end, stops, turns, parking, idling, GPS gaps, tunnels, false jumps.
- Work assumed by default; personal exclusions and home-to-home group exclusions.
- Automatic shift grouping, manual corrections, audit trail, work mileage and reports.
- Full track map, trip timeline, export GPX/CSV/GeoJSON, encrypted backups.
- Route reconstruction/map matching and optional navigation instruction capture.

## P1: Visual layout
- Brand Montrix, icon, splash screen, fonts and spacing.
- RadioText top, station name bottom, frequency separate; full-width non-clipped marquee.
- Hide radio Play and volume controls; FM seek and presets instead.
- Active audio source indicators; map left/right layout and photo.
- Current speed, regulatory speed-limit sign, outside temperature in correct order.
- Speed warning colors, 1280x800 scaling, night theme, button hit targets.
- Apps, phone, navigation, guest BT and vehicle controls.

## P2: Navigation and vehicle
- Offline Texas speed limits, school-zone schedules, time/holiday rules, warnings.
- Expand offline DB to other states; speed limit transition reliability.
- GPS follow/recenter, route line, zoom, map cache.
- Real Ford CAN profile, fuel, range, coolant, oil, battery, TPMS, warnings.
- Prevent simulator values from being mistaken for vehicle telemetry.

## P2: Engineering
- Preserve last-known-good APK, build logs, version/hash, rollback.
- Per-module tests, device smoke tests, ADB diagnostic capture for 12-hour shifts.
- Human-readable Java, stable interfaces, no monolithic source rewrites.
