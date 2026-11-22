# Build instructions

Windows PowerShell (from this project directory):

```powershell
& "D:\platform-tools-latest-windows\gradle-8.9\bin\gradle.bat" --no-daemon :app:assembleDebug
```

Ensure `local.properties` contains your SDK path. This release intentionally has no automatic deployment: review, compile and test before installing. Existing Android applicationId `com.monte.cwgradio` is preserved to avoid an accidental new-app install. Back up your data before any update.

Build success and hardware correctness are separate: physically test tuner, ignition, music, GPS and RDS.
