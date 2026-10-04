# V50S controller Implementation Plan

> Execute inline in this session using executing-plans. No agent delegation required.

**Goal:** Select an installed emulator in ReGene and automatically expand its game at rotation3: cover top, body bottom.
**Architecture:** Native launcher, foreground session service, usage events, LG ActivityManager reflection. Persistent rotation snapshot, cover check, bounded retry budget and visible errors.
**Tech Stack:** Java8 source, Android SDK36 compiler tools, device API31; no third party runtime dependencies.

- [ ] Create app/AndroidManifest.xml and app/res/values/strings.xml with four package queries, foreground-service, usage-access and write-settings declarations.
- [ ] Create app/src/dev/regene/v50s/MainActivity.java: display four installed apps; settings links; launch selected app after starting controller. Include stop action and status.
- [ ] Create LgWide.java: invoke getWideScreenMode/setWideScreenMode on activity service; propagate exceptions to log and UI.
- [ ] Create ControllerService.java: foreground notification, usage resume event detection, selected-app session, game Activity transition observation, cover existence, rotation3, retry maximum6 per15s then60s cooldown. HOME disables expansion and restores previous settings. Stop removes callbacks and restores session state.
- [ ] Create app/build.ps1 to compile with javac --release8, D8, aapt package, zipalign, debug signing. APK signature check must pass.
- [ ] Install via adb install -r app/build/regene-debug.apk; enable required first-run permissions using Android settings. Run launcher, select Citra, enter Kirby, capture logical size and app logs. HOME/return must restore automatically without WideMode UI.
- [ ] Verify cold start and same-process return, exact lower game touch. Record limits explicitly for melonDS controls, DraStic sensor handling and Azahar virtual-display conflict.
- [ ] Verify manual stop restores settings and no polling remains; document build/run steps and outstanding physical/USB-disconnected tests in README.md.

Acceptance never relies only on an API return: inspect wide state and2340x2160 logical dimensions; include real touch/game response. Azahar repeated resets trigger cooldown, not unbounded resize loops. Only mark full goal complete after all four apps and physical/USB-off transitions pass.
