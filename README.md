# cleanyourmobile

No-root debloat, RAM/battery tuning and DNS ad blocking for the **OnePlus 6T** (A6013 / A6010, OxygenOS).

Two ways to use it:

1. **Mac/PC script** (`scripts/cleanmobile.sh`) - recommended, most reliable.
2. **Android app** (`app/`) - runs the same lists on the phone through [Shizuku](https://shizuku.rikka.app/).

`pm uninstall -k --user 0` removes an app for user 0 only. The APK stays in /system, so it is **fully reversible** (`restore`) and a factory reset brings everything back.

## Quick start (macOS)

```bash
brew install android-platform-tools
git clone https://github.com/vinothkumarpughendran-collab/cleanyourmobile.git
cd cleanyourmobile && chmod +x scripts/*.sh
```

On the phone: Settings > About phone > tap *Build number* 7x > Settings > System > Developer options > enable **USB debugging**. Plug in, accept the RSA prompt, then:

```bash
adb devices                              # must show 'device'
scripts/cleanmobile.sh debloat-safe      # Facebook, Netflix preload, OnePlus junk
scripts/cleanmobile.sh tune              # animations, dexopt, cache trim, AdGuard DNS
scripts/cleanmobile.sh debloat-google    # optional Google apps (review list first)
scripts/cleanmobile.sh debloat-caution   # OnePlus apps you may use (review first)
scripts/cleanmobile.sh restore lists/oneplus_safe.txt
scripts/cleanmobile.sh undo-tune
```

Every debloat run first saves a package snapshot to `backups/`.

## What it does

| Goal | Method |
|---|---|
| Debloat | `pm uninstall -k --user 0` for packages in `lists/` |
| Faster UI | Animation scales 0.5x |
| Faster apps / RAM | Kill background apps, trim caches, background dexopt (`speed-profile`) |
| Battery | Fewer preloaded background apps and services, Adaptive Battery/App Standby enabled |
| Ads / trackers | System-wide Private DNS = `dns.adguard-dns.com` |

No tool can truly 'boost RAM'. Android manages RAM itself; the real gain comes from removing apps that autostart.

## Android app (experimental, untested build)

1. Install Shizuku from Play Store/GitHub on the phone and start it: Developer options > Wireless debugging > in Shizuku choose *Start via Wireless debugging* (re-do after each reboot).
2. Open `app/` in Android Studio (Koala+), let Gradle sync, *Build > Build APK*.
3. Install the APK, open it, grant Shizuku permission, tick packages, tap **Debloat selected**.

## Safety

- Package names vary by OxygenOS version; missing ones are skipped.
- Never remove: `net.oneplus.launcher`, `com.oneplus.camera`, `com.oneplus.deskclock`, `com.android.phone`, `com.google.android.gms`, `com.android.systemui`.
- Cell-broadcast (emergency alerts) is deliberately not listed.
- If the phone misbehaves: `scripts/cleanmobile.sh restore <list>` or `adb shell cmd package install-existing <pkg>`.
- Use at your own risk.
