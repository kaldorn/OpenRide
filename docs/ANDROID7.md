# Android 7 fork (original Peloton Bike, PLTN-RB1VO)

This branch lowers OpenRide's `minSdk` from 29 to 24 so it installs on the original Bike's
tablet, which Peloton never updated past Android 7.0.

## What changed

| Change | Why |
|---|---|
| `minSdk` 29 → 24 | The PLTN-RB1VO tablet runs Android 7.0 (API 24) |
| Core library desugaring | `java.time` (ride history, exports, feed dates) only exists from API 26 |
| `java.util.Base64` → `android.util.Base64` | `java.util.Base64` is API 26 |
| `LegacyFileAutoBackupStore` | `MediaStore.Downloads` is API 29; below that the backup is a plain file in `Download/OpenRide/` |
| `WRITE_EXTERNAL_STORAGE` (maxSdk 28) | Needed for that file on Android 7-9 |
| Update asset `api24` | Upstream's self-update APKs need Android 10, so the fork never offers them |
| Committed debug keystore | Every CI build has the same signature, so `adb install -r` upgrades in place |
| `.github/workflows/build-api24.yml` | Builds the APK in GitHub Actions |

The sensor path needs no change: the original Bike exposes the same
`com.onepeloton.affernetservice` / `IV1Interface` binder that Grupetto uses on it.

## Building

Actions tab → **Build Android 7 APK** → **Run workflow**. When it finishes, download the
`openride-api24` artifact (a zip containing the APK).

## Installing (Windows, from `C:\platform-tools`)

```
.\adb install -r .\openride-api24-<run>.apk
.\adb shell pm grant dev.digitalducktape.openride.real android.permission.WRITE_EXTERNAL_STORAGE
.\adb shell pm grant dev.digitalducktape.openride.real android.permission.CAMERA
.\adb shell monkey -p dev.digitalducktape.openride.real 1
```

The two `pm grant` lines are optional: the first keeps the automatic backup in
`Download/OpenRide/` (otherwise it lives in the app's own folder and is lost on uninstall),
the second pre-approves the camera for the head-lean games.

## Known risks on this tablet

- **Class videos** play in the system WebView (Chromium 91 on the tested tablet). If YouTube's
  embedded player refuses it, live stats, games and history still work.
- **Untested on hardware** until someone rides it. Report results in this fork's issues.
