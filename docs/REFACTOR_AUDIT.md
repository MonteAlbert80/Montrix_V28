# Refactor audit

## Removed
- `RadioState.java`: no source references; no manifest entry; safe at static-reference level only.
- Historical one-off `install_v*.ps1`, `BUILD_V*.ps1`, `README_V*.txt`, test scripts and release-note fragments: not part of Gradle application build.
- Exact duplicate theme folder: only if byte-for-byte identical.

## Intentionally retained
- `MainActivity`, `RadioActivity`, `MusicActivity`, `ToggleActivity`: manifest components; external shortcuts may rely on them.
- `BootReceiver`: registered boot integration.
- `NativeRadioService`, `NativeRdsReader`, `SyuRadioClient`: tuner hardware bridge.
- `OfflineTexasMapView`, `FytEmbeddedActivityHost`, `InstrumentDrawable`, `SimVehicleState`, `VehicleTemperatureProvider`, `DiagnosticLogger`: referenced in dashboard or runtime.
- `launcher_theme`: optional CarWebGuru interoperability, not Montrix brand.

## Migration
- Java package and Android Gradle namespace are `com.monte.montrix`.
- Android applicationId and external broadcast action strings intentionally remain `com.monte.cwgradio` for backward compatibility. These are compatibility identifiers, not brand names.
- Without Android SDK/Gradle or physical FYT unit, cannot certify binary equivalence. Build and physical regression tests are required.

## Regression checklist
1. Install as upgrade (no uninstall), verify existing data/settings and app launcher.
2. Cold boot/ACC wake, dashboard auto-launch, OEM radio foreground behavior.
3. Radio tune, presets, seek, RDS PS/RT, full scrolling.
4. Music source switching, ignition sleep, seamless cranking, resume.
5. GPS map, offline map, simulator isolation, raw GPS trip journal.
6. CarWebGuru theme and shortcuts; CAN temperature and vehicle warnings.
