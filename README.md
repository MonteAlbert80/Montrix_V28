# MONTRIX v27 — Clean Refactor Candidate

**Status: source-only, not compiled or verified on the head unit.**

## What changed
- Java namespace moved from `com.monte.cwgradio` to `com.monte.montrix` throughout.
- Android `applicationId` **remains `com.monte.cwgradio`** to preserve installed app identity and data.
- Legacy broadcast action strings retain `com.monte.cwgradio.*` for external simulator/vehicle tooling compatibility.
- All Android manifest components now have explicit fully qualified Montrix class names.
- Removed the unreferenced `RadioState.java` class (zero references outside its own declaration).
- Removed obsolete version-specific installers, README fragments, and duplicate theme files when byte-identical.
- Preserved the optional CarWebGuru launcher theme under `launcher_theme/`; this is third-party compatibility content, not product branding.
- Kept all live Android entry points and OEM radio hardware code. Removing them without device tests risks breaking functionality.
- Preserved `DashboardActivity`, `NativeRadioService`, tuner, GPS and map integration while separating new RDS and trip storage modules.

## Architecture limits
This is a **safe-first migration**, not a completed 130-class decomposition. The legacy dashboard and radio service are still large. The next step is method-by-method extraction backed by device regression testing. No claim is made that old code is dead without reference or hardware analysis.

## Build
Build with the installed Android Gradle plugin/SDK on your Windows machine:
`gradle --no-daemon :app:assembleDebug`
Run `python tests/check_source.py` first.

## Compatibility and security
Application ID unchanged. Broadcast actions unchanged. SQLite schema must be migrated, never dropped. Existing app's launcher theme integration is retained. GPS journal remains local.
