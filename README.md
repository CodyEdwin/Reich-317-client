# Reich 317 — Client

Game client for the Reich 317 server: an Elvarg 3.2 / RuneLite based `317`
client with a full **Android port** alongside the original desktop client.

- Cache data: **206**
- Base: [Elvarg Client 3.2](https://www.rune-server.ee/runescape-development/rs2-server/downloads/660901-economy-version-elvarg-framework-some-skills-improved-core.html)
- Android port details and status: see [`PORTING.md`](PORTING.md)

## Layout

```
.
├── src/main/java/     Shared client sources (com.runescape.*, net.runelite.*)
├── Cache/             Game cache (main_file_cache.dat + idx files) — required to run
├── android/           Android app module (com.elvarg.android, minSdk 26)
├── buildSrc/          Gradle build helpers
├── build.gradle.kts   Desktop build (Java 11, JavaFX, main class RuneLite)
└── PORTING.md         Notes on porting the AWT/Swing client to Android
```

## Requirements

| Target | Needs |
| --- | --- |
| Desktop | JDK 11+, Gradle wrapper 7.4.2 (bundled) |
| Android | JDK 17, Android SDK 35, Gradle wrapper 8.10.2 (bundled in `android/`) |

## Build — desktop

```bash
./gradlew build          # compile + assemble
./gradlew run            # launch the client (net.runelite.client.RuneLite)
```

## Build — Android

The SDK location is read from `android/local.properties` (`sdk.dir=...`).

```bash
cd android
./gradlew :app:assembleDebug
```

Output: `android/app/build/outputs/apk/debug/app-debug.apk`

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

The APK does not bundle the cache — copy the repository `Cache/` folder to the
app's storage directory (the same layout as on desktop) before first launch.

## Features

- Textures, HD and GPU rendering paths
- RuneLite plugin, overlay and config API with an in-game menu on Android
- Sprite and model loading from the 206 cache (`main_file_cache.dat`)
- RuneLite launcher support
  ([setup](https://github.com/Mark7625/Runelite-Launcher-rsps/blob/main/SETUP.MD))
- Login screen written in Kotlin (`loginscreen/LoginScreen.kt`) for Android
- Shared source tree: desktop and Android compile the same `src/main/java`,
  with AWT/applet/Swing classes swapped for Android equivalents (see
  `PORTING.md`)

## Credits

- [Professor Oak](https://www.rune-server.ee/members/professor+oak/)
- [Mark](https://www.rune-server.ee/members/Mark_/)
