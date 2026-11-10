# Porting the Elvarg/RuneLite client to Android

This document tracks the Android port of the desktop client in this repository.
It mirrors the approach already used by the separate `317-world` project: a
Gradle Android module lives under `android/`, reuses the shared `src/main/java`
tree, and replaces the desktop-only (AWT/applet/Swing/GPU) classes with Android
equivalents that keep the same package and class names.

The goal (agreed with the project owner) is **full feature parity**: the client
itself boots and plays, and the RuneLite plugin/overlay/config features work —
but the RuneLite *desktop Swing UI* is replaced by an **Android in-game menu**.

> Status legend: **[x]** done, **[~]** in progress, **[ ]** not started.

---

## 1. Why this is more involved than the 317 port

In `317-world`, `client.java` is already AWT-free: the AWT lived only in
`RSApplet`/`RSImageProducer`/`Main`, which the Android module replaced wholesale.
The client compiled unchanged.

This client is a modern RuneLite/Elvarg client and is **not** AWT-free:

* `com.runescape.Client extends com.runescape.engine.GameEngine`, and
  `GameEngine extends java.applet.Applet` and uses AWT heavily.
* `com.runescape.Client` itself imports `java.awt.*`, `javax.imageio.*`,
  `java.awt.image.BufferedImage`, `java.awt.event.KeyEvent`.
* The RuneLite API layer is AWT-based: `net.runelite.api.hooks.Callbacks` takes
  `MouseEvent`/`KeyEvent`/`MouseWheelEvent`, `net.runelite.api.Client.getCanvas()`
  returns `java.awt.Canvas`, overlays draw with `java.awt.Graphics2D`, etc.
* The RuneLite host (`net.runelite.client.*`) is a Guice + Swing application,
  and the GPU plugin (`plugins/hd`, `plugins/gpu`) uses LWJGL/OpenGL/JOCL.

**Android cannot define or load classes in `java.*`.** You cannot ship a
`java.awt` stub in an APK — the ART class loader refuses the package
(`Prohibited package name: java.awt`). Therefore *every compiled class that
references `java.awt`/`java.applet`/`javax.swing`/`javax.imageio` must be
replaced by an Android variant*, and the RuneLite API/events that are typed in
terms of AWT must be re-typed in terms of Android-neutral types.

---

## 2. Module layout

```
android/
  settings.gradle / build.gradle / gradle.properties / gradlew(+.bat)   [x]
  app/
    build.gradle                 # AGP app module; Syncs + excludes shared src [x]
    src/main/AndroidManifest.xml                                        [x]
    src/main/res/values/{strings,styles}.xml                            [x]
    src/main/java/com/elvarg/android/     # app + platform layer
      AndroidDisplay.java        # master 0x00RRGGBB framebuffer             [x]
      KeyMap.java                # Android KeyEvent -> AWT-style key codes    [x]
      GameView.java              # framebuffer -> Bitmap, touch -> input      [x]
      MainActivity.java          # host + Android RuneLite menu               [x]
      ClientService.java         # foreground service, cache unpacking       [x]
      ClientEngine.java          # boots com.runescape.Client                 [x]
      AndroidCallbacks.java      # no-op/plugin Callbacks impl                [x]
    src/main/java/com/runescape/...        # same-package replacements
      engine/GameEngine.java                                                [x]
      engine/Canvas.java                                                    [x]
      engine/impl/{MouseHandler,KeyHandler,MouseWheelHandler}.java          [x]
      draw/ProducingGraphicsBuffer.java                                     [x]
      sign/SignLink.java                                                    [x]
    src/main/java/net/runelite/...         # AWT-free API variants           [~]
```

`app/build.gradle` uses a `Sync` task (`prepareShared`) to copy
`../../src/main/java` into the build dir **excluding** the classes this module
replaces. This is required because Gradle `exclude` patterns are global across
source dirs, so a same-relative-path replacement (e.g.
`com/runescape/engine/GameEngine.java`) cannot be both kept and dropped.

The cache is packaged from `../../Cache` as Android assets and unpacked to
internal storage on first run (`ClientService`), because the client opens it
with `RandomAccessFile`.

---

## 3. AWT / platform dependency inventory

Files under the non-framework tree that reference AWT/applet/Swing/ImageIO
(each needs an Android variant, or an in-place AWT removal in a copied variant):

### com.runescape (28)
```
Client.java                 RSFont.java                Sprite.java
SpriteCache.java            ProducingGraphicsBuffer.java  Rasterizer2D.java
WheelOfFortune.java         skillorbs/SkillOrb.java    skillorbs/SkillOrbs.java
teleports/HierarchyOption.java  teleports/TeleportChatBox.java
engine/Canvas.java          engine/GameEngine.java
engine/impl/KeyHandler.java engine/impl/MouseHandler.java
engine/impl/MouseWheelHandler.java
entity/GameObject.java      entity/GroundItemTile.java entity/Npc.java
entity/Player.java          entity/model/Model.java
model/content/Keybinding.java
scene/object/{GroundDecoration,WallDecoration,WallObject}.java
sign/SignLink.java          util/FileUtils.java        util/SystemUtils.java
```

### net.runelite.api / net.runelite.rs.api (non-framework) (30)
```
api/Actor.java              api/Client.java            api/Constants.java
api/DecorativeObject.java   api/GameEngine.java        api/GameObject.java
api/GroundObject.java       api/JagexColor.java        api/MainBufferProvider.java
api/Perspective.java        api/Player.java            api/SpritePixels.java
api/TileObject.java         api/WallObject.java
api/geometry/{Geometry,Shapes,SimplePolygon}.java
api/hooks/Callbacks.java
api/queries/{BankItemQuery,InventoryWidgetItemQuery,ShopItemQuery}.java
api/widgets/{Widget,WidgetItem}.java
rs/api/{RSGameEngine,RSKeyHandler,RSModel,RSMouseHandler,RSMouseWheelHandler,RSRasterProvider}.java
```

### net.runelite.client framework (~136 of 194 AWT-touching files)
Everything under `net/runelite/client/ui/**` (Swing `ClientUI`, `SplashScreen`,
`FatalErrorDialog`, plugin panels, navigation button), `discord/**`,
`plugins/hd/**`, `plugins/gpu/**`, `plugins/model-viewer/**`, `Notifier.java`,
`callback/Hooks.java` (draws with `Graphics2D`). These are replaced by an
Android menu + an Android overlay renderer rather than ported verbatim.

### Notable AWT symbols that must be re-typed
| Desktop (AWT)                     | Android replacement                          |
|-----------------------------------|----------------------------------------------|
| `java.awt.Canvas`/`Component`/`Image` | `Object` / `android.graphics.Bitmap`     |
| `java.awt.Graphics`/`Graphics2D`  | `android.graphics.Canvas` (overlays/menu)    |
| `java.awt.Point`/`Rectangle`/`Dimension`/`Shape`/`Polygon` | `net.runelite.api.Point` + small geometry types in `com.elvarg.android.geom` |
| `java.awt.Color`                  | int ARGB                                      |
| `java.awt.event.MouseEvent`/`KeyEvent`/`MouseWheelEvent` | Android-neutral `PointerEvent`/`KeyStroke` in `com.elvarg.android.input` |
| `javax.imageio.ImageIO`/`Toolkit` | `android.graphics.BitmapFactory`/`Bitmap.compress` |
| `javafx.beans.property.*` (login fade) | tiny own property holder                |
| `java.applet.Applet`              | plain class                                   |

---

## 4. Phased plan

### Phase 0 — scaffold [x]
Android Gradle module, manifest, resources, wrapper, `Sync`-based shared source
inclusion with the exclude list, app id, `AndroidDisplay`, `SignLink`,
`KeyMap`.

### Phase 1 — software render + boot the client [x]
1. **[x]** `AndroidDisplay` master framebuffer.
2. **[x]** `com.runescape.sign.SignLink` (cache dir)
3. **[x]** `com.runescape.draw.ProducingGraphicsBuffer` → composite into
   `AndroidDisplay` (drop `BufferedImage`).
4. **[x]** `com.runescape.engine.GameEngine` and `engine.Canvas` → AWT-free game
   loop (tick + graphics tick), driving `AndroidDisplay`, no `Applet`/`Frame`.
5. **[x]** `engine.impl.{MouseHandler,KeyHandler,MouseWheelHandler}` → Android
   input, keeping every static field `Client.java` reads.
6. **[x]** `ClientService`, `ClientEngine`, `GameView`, `MainActivity` to boot
   `com.runescape.Client` and display it.
7. **[x]** Port the AWT-touching client files that the login/scene path needs:
   `Rasterizer2D`, `Sprite`, `SpriteCache`, `RSFont`, `Model`, `Player`, `Npc`,
   `GameObject`, `Keybinding`, `GroundItemTile`, `scene/object/*`,
   `draw/skillorbs/*`, `draw/teleports/*`, `WheelOfFortune`, `FileUtils`,
   `SystemUtils`, and the small `Client.java` AWT spots (map dump, error
   screen, `KeyEvent` constants).

Verified on the desktop JVM with an AWT/ImageIO-backed `android.graphics`:
cache opens (2082 maps, 67136 models, 645 sprites, 27141 items), the login
screen draws (background, logo, EULA dialog, world list), and synthetic
press/release/key events drive it (accept EULA → login → typing → world list
→ cancel).

### Phase 2 — AWT-free RuneLite API layer [x]
Android variants of all 30 `net.runelite.api`/`rs.api` AWT-touching types.
`com.elvarg.android.input.{PointerEvent,KeyStroke,KeyCode}` and
`com.elvarg.android.geom.*`/`gfx.*`/`beans.*` replace the AWT/JavaFX types,
keeping the same simple names wherever possible. `Callbacks`,
`Client.getCanvas()`, `MainBufferProvider.getImage()`, `Perspective`, `Actor`
and `Constants` are re-typed. `AndroidCallbacks` (inert pass-through) is
installed into `Client`.

### Phase 3 — RuneLite framework without Swing [~]
The desktop host (`net.runelite.client.RuneLite`, Swing UI, Guice injector) is
still excluded wholesale, but the **plugin/overlay runtime is now up** with
Android-native implementations in the same packages, so ported plugins compile
against the usual `net.runelite.client.*` API:

* `eventbus/{EventBus,Subscribe}` — same semantics, reflection dispatch instead
  of `LambdaMetafactory`/`MethodHandles` (Android's `java.lang.invoke` support is
  partial). Priorities, `post`, `register`/`unregister` unchanged.
* `config/{Config,ConfigGroup,ConfigItem,Range}` — annotations kept;
  `ConfigBase` + `ConfigManager` store values as JSON
  (`runelite-config.json` in the data dir) and post `ConfigChanged`.
  **Configs are classes, not interfaces**: resolving an interface's default
  method needs Java 9 `MethodHandles.privateLookupIn`, which Android lacks.
* `plugins/{Plugin,PluginDescriptor,PluginManager}` — lifecycle, enabled-state
  persistence (`runelite` config group, `plugin.<SimpleName>`), event-bus
  subscription on start/stop. Dependencies come from
  `com.elvarg.android.rl.AndroidInjector` (constructor/field `@Inject`), not a
  Guice child injector. `getInjector()` returns `null`.
* `ui/overlay/{Overlay,OverlayManager,OverlayRenderer,OverlayUtil}` +
  `OverlayLayer/Position/Priority` — draws straight into the framebuffer through
  the neutral `Graphics2D`; colours are ARGB ints, geometry uses
  `com.elvarg.android.geom.*`.
* `ui/DrawManager` — per-frame listeners; snapshot consumers receive an Android
  `Bitmap` instead of `java.awt.Image`.
* `com.elvarg.android.rl.AndroidPluginHost` — the Android replacement for the
  RuneLite host: builds the services, registers the shipped plugins and starts
  them lazily on the game thread (first tick/frame, after `Client.startUp()`).
* `AndroidCallbacks` now forwards `post`/`tick`/`frame` to the bus and renders
  overlays from the draw hook.

First ported plugin: **FPS Control** (`plugins/fps`, indicator only — the
desktop FPS limiter is not ported). Verified in the desktop harness: the host
starts with 1 plugin / 1 overlay and the frame shows `NN FPS` in the top-right.

**Not yet ported:** the rest of `net/runelite/client` (Swing `ui/**`,
`discord/**`, `Notifier`, external plugin loading, session/account services) and
the plugin hub. Those are excluded; add them package by package together with
same-package Android variants.

### Phase 4 — overlay + menu UI [x]
Overlay rendering is **done** (see Phase 3): overlays are painted over the
blitted frame inside the `AndroidDisplay` lock, so a displayed frame always
contains its overlays.

The Android in-game menu replaces the RuneLite sidebar
(`com.elvarg.android.GameMenu` + `ConfigEditor`, opened from the **Menu**
button):

* **Plugins** — lists `PluginManager.getPlugins()` with a checkbox per plugin;
  toggling calls `setPluginEnabled` and persists the enabled state.
* **Settings** — reflects over every loaded `ConfigBase` and renders native
  controls from the `@ConfigItem` metadata: checkboxes for booleans, seek bars
  for `@Range` ints, text fields for other numbers/strings, spinners for enums
  and a swatch + hex field for `@ConfigItem(color = true)` items. Values are
  written through `ConfigManager` (so `ConfigChanged` fires) and a Reset
  button clears the group. The config/plugin name is resolved from the
  plugin's static `CONFIG_GROUP` key.
* **World switcher** — lists the `WorldManager` world list and updates
  `WorldManager.selectedWorld` / `Client.server`; switching while logged in logs
  out first.
* **Log out**, **Keyboard**, **Right-click** and **Quit**.

Actions that mutate client or plugin state are queued with `ClientEngine.post`
and drained at the start of the next tick/frame, so plugin lifecycle and config
events always run on the game thread. Dialog widgets are built in code (no
AndroidX dependency).

### Phase 5 — plugins [~]
Three of the four portable plugins are ported; the GPU/HD plugins (`lwjgl`/
`jocl`) need OpenGL ES and stay excluded, so the software rasteriser is used.

* **FPS Control** (`plugins/fps`) — the frame-rate indicator *and* the
  frame-rate limiter (a draw listener that sleeps for the remainder of the
  frame time, exactly like on desktop). The app tells the client when it goes
  to the background, so the "unfocused" limit applies.
* **Entity Hider** (`plugins/entityhider`) — the client already implements the
  hide flags, so the plugin is the same config-to-client bridge as on desktop.
  13 boolean settings, editable from the in-game menu.
* **Minimap** (`plugins/minimap`) — dot colours, hide minimap and minimap
  zoom. Colours are ARGB ints (no `java.awt.Color` on Android) and zooming
  listens for the Android `MouseWheelMoved` event instead of RuneLite's AWT
  `MouseManager`; the client has no script system, so the "hide minimap"
  update happens on config change instead of `ScriptPostFired`.
* **Stretched Mode** (`plugins/stretchedmode`) — the desktop plugin resizes an
  AWT canvas that does not exist on Android, so this variant drives the *view*
  scaling instead: keep the aspect ratio or stretch to fill, whole-number
  scale factors, unfiltered (faster) scaling and how much of the screen the
  game uses. The options live in `com.elvarg.android.ViewScaling` and are
  picked up by `GameView` every frame.

`plugins/stretchedmode`'s desktop mouse-translation listeners are not ported:
`GameView` already maps touch/mouse coordinates into the client canvas.

### Phase 6 — build & verify [~]
`cd android && ./gradlew :app:assembleDebug`. The module now builds against a
real Android SDK (`local.properties` → `sdk.dir`). Two host-specific details:

* This environment runs on aarch64, but AGP downloads the x86_64 `aapt2` from
  Maven. Gradle therefore uses the SDK's native `aapt2` through
  `android.aapt2FromMavenOverride` (set in `android/gradle.properties`).
* `/sdcard` is mounted `noexec`, so `build-tools` cannot be executed from the
  installed SDK directly. The build uses a `/tmp/opencode/android-sdk` mirror
  whose `build-tools` are copied (and executable) and whose `platforms`/
  `platform-tools` are symlinked. This is only needed on this host; a normal
  desktop SDK path works unchanged.

The port also replaces the desktop `javax.sound.sampled` player with an
`AudioTrack` implementation (`com.runescape.sound.SoundPlayer`) so the client's
WAV music/effects compile and play on Android.

What is verified here: a custom harness compiles the module's shared + Android
sources against real dependency jars (**795 Java, 0 Kotlin, 0 errors**), and a
second harness boots the client on the desktop JVM with an AWT/ImageIO-backed
`android.graphics`. It confirms cache loading, the login screen (background,
logo, login box, world list), the RuneLite runtime (3 plugins loaded, FPS
overlay in the top-right) and the settings/world data written by the Java
ports. The APK is built with `assembleDebug`; installing/launching it still
needs a device or emulator.

### Installing the debug APK

```
cd android && bash gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The APK is ~113 MB because the Jagex cache ships as an asset; it is unpacked
into the app's private storage on first run.

---

## 5. Key coupling facts (for whoever continues this)

* `Client.instance` is set in `Client.init()`; the loop is started by
  `startThread(765, 503, 206, 1)` → `GameEngine.run()`.
* `Client` reads from the engine: `canvas`, `canvasWidth`, `canvasHeight`,
  `GameEngine.method2692()`, `super.fps`, `super.drawInitial(...)`,
  `setMaxCanvasSize(...)`, `rasterProvider` (static on `Client`).
* `Client` reads from input handlers (all `static`): `MouseHandler.mouseX/mouseY/
  saveClickX/saveClickY/clickMode3/idleCycles/lastPressed/lastMoved/instance`;
  `KeyHandler.instance.keyArray`, `KeyHandler.instance.readChar()`,
  `KeyHandler.idleCycles`; `MouseWheelHandler.mouseWheelDown`.
* `Client.callbacks` (private) is used unconditionally (`callbacks.post`,
  `callbacks.tick`, `callbacks.frame`, `callbacks.drawInterface`) → it must be
  non-null (`AndroidCallbacks`).
* `Client` uses `java.awt` in only a handful of spots (map-region dump,
  `showErrorScreen`, `KeyEvent.VK_SPACE`, `getExtendedKeyCodeForChar`) — an
  Android variant needs only small edits.
* Login fade uses `javafx.beans.property.{DoubleProperty,SimpleDoubleProperty}`
  in `LoginScreen.kt` and `CinematicScene.java`.
* `WorldManager.kt` fetches the world list with OkHttp; `UserPreferences.kt`
  persists preferences with Klaxon (both Android-friendly).

### Android-only behaviour changes

These are deliberate differences from the desktop tree; the desktop sources are
untouched, the Android variants live in `android/app/src/main/java`.

* **World list data.** Desktop reads `./worldData.json` from the working
  directory, which does not exist for an app. `prepareAssets` copies it into the
  APK assets, `ClientService` unpacks assets into the cache directory, and the
  Android `WorldManager` reads `worldData.json` from `SignLink.findcachedir()`
  (falling back to the working directory, then to a built-in world). A missing
  or malformed file can no longer crash the client.
* **World-select input.** Desktop handles the world-list Cancel/world clicks in
  `renderWorldSelect()`, i.e. during the graphics tick. A click is only visible
  in `MouseHandler.clickMode3` until the next client tick, and the game loop
  runs catch-up client ticks before it draws whenever a frame takes longer than
  20 ms — routine on Android — so most presses were dropped. The Android
  `WorldManager.handleInput()` runs those checks from
  `LoginScreen.handleInput()` (client tick) instead.
* **`hasFrame()`** is always `false` (there is no `Frame`); `Client` uses
  `GameEngine.getContentWidth()/getContentHeight()` instead of the canvas
  parent's size.
* **Sprite decode failures are non-fatal.** `Sprite` swallows a failed
  `BitmapFactory` decode and `ImageCache.get()` returns a 0×0 sprite until the
  sprite is loaded; unchanged from desktop, but it means image-sized click
  regions are empty until the sprite arrives.
* **Frames are presented atomically.** The game thread composes the frame
  (game + overlays) in a bitmap and swaps it in while holding
  `AndroidDisplay.LOCK`; the UI thread draws that bitmap under the same lock,
  so it can never display a frame that was captured between the blit and the
  overlay pass.
* **The frame is only uploaded once.** The overlay pass draws straight onto
  the bitmap that is presented (instead of copying the framebuffer into a
  scratch bitmap, drawing there and copying back), and the view draws that
  bitmap directly. That removes three full-frame pixel copies (1.5 MB each)
  per frame compared to the first version of the port.
* **RuneLite runtime starts on the game thread.** `AndroidPluginHost` is
  created from the first tick/frame callback (after `Client.startUp()`), not
  from `ClientEngine`, so plugins never see a half-initialised client.
* **RuneLite configs are classes.** `ConfigBase` + `ConfigManager` replace the
  interface/default-method/dynamic-proxy design; values are JSON in
  `runelite-config.json` next to the cache.
* **Guice is only used for source compatibility.** `Plugin implements Module`
  and exposes `getInjector()` (always `null`); dependencies are injected by
  `AndroidInjector`. The dependency is `guice:4.2.3:no_aop` — 5.x has no
  `no_aop` variant on Maven Central, which would break the Gradle resolve.
* **The RuneLite sidebar is an Android dialog menu.** Plugin enable/disable,
  config editing, world switching and logout live in `GameMenu`/`ConfigEditor`,
  not Swing. Menu actions are posted to the game thread. There is no plugin
  hub or external plugin loading.
* **The client-wide settings live in `RuneLiteConfig`** (group `runelite`,
  the same group as the enabled-plugin state): show the on-screen button bar
  and enable/disable plugin notifications. The desktop config is mostly about
  the Swing window, so only the settings that still apply were kept.
* **Notifications are system notifications.** The desktop notifier uses a
  system-tray balloon; on Android the `Notifier` keeps a history (listed in
  the in-game menu) and posts the message to the notification shade.
* **Overlay right-click menus are dialogs.** `Overlay.getMenuEntries()` and
  the `OverlayMenuClicked` event are ported; a right click (or a long press)
  on an overlay opens a dialog with its entries instead of injecting them
  into the game's own right-click menu.
* **Touch input.** A press is a left click and the **Right-click** toggle makes
  the next press a right click. Held movement is delivered as a mouse drag
  (interface sliders, camera). A two-finger vertical drag generates wheel
  rotations (chat/interface scrolling and zoom).
* **Audio uses `AudioTrack`.** The desktop `SoundPlayer`'s `javax.sound.sampled`
  implementation is replaced by one that parses the client's WAV data and
  streams 16-bit mono samples; volume uses the same decibel curve.
* **Chat messages.** The desktop client leaves RuneLite's chat API
  unimplemented, but this client has a chat box of its own, so
  `Client.addChatMessage` now forwards the message into it and the RuneLite
  chat classes (`ChatMessageBuilder`, `ChatMessageManager`, `ChatColor`,
  `QueuedMessage`, `ColorUtil`, `Text`) are ported. The client's font
  renderer understands the `<col=...>` tags the builder produces.
* **Chat commands.** Typing {@code //command} in the chat runs a plugin
  command instead of sending the message to the server. The desktop client
  hooks into the OSRS chatbox script, which this client does not have, so the
  Android port intercepts the message in the client's own chat input. The
  commands {@code //plugins} and {@code //commands} are built in.
* **Registry/browser integrations are absent.** Discord, the hiscore/session
  services and Swing-only panels have no Android host yet.

---

## 7. Open items (in priority order)

1. ~~**Bluetooth mouse / keyboard support.**~~ **Done.** `GameView` now
   separates pointer devices from fingers: `MotionEvent.getButtonState()`
   picks the button (`BUTTON_SECONDARY` → right click, so the on-screen
   right-click toggle is no longer needed for a mouse), `ACTION_HOVER_MOVE`
   moves the cursor without clicking (via an `OnHoverListener`, which is also
   what makes the view hoverable) and `ACTION_SCROLL` becomes a wheel
   rotation. The on-screen toggle stays as the fallback for touch-only
   devices. Key events were already forwarded to the client.
2. ~~**Kotlin → Java.**~~ **Done.** The Android module contains **no Kotlin
   sources**: the shared Kotlin files (`UserPreferences`, `LoginBackground`,
   `LoginState`, `WorldData`, `WorldType`) and the Kotlin Android variants
   (`LoginScreen`, `WorldManager`, `FlameImages`, `ImagePacker`) were ported
   to Java with the same package and class name and are listed in
   `sharedExcludes`. The Kotlin Gradle plugin was dropped from the module.
   One consequence: the settings file is written with Gson instead of Klaxon
   (Klaxon's API is inline/reified and cannot be called from Java) — the JSON
   format is the same.
3. ~~**Stretched mode.**~~ **Done** — re-implemented as a view setting (see
   Phase 5).
4. **Colour configs in the settings editor** are a hex field plus a swatch;
   a proper colour picker (or a palette of the RuneLite defaults) would be
   nicer for the Minimap dot colours.
5. **GPU / HD plugins** (`plugins/gpu`, `plugins/hd`) need OpenGL ES and are
   still excluded; the software rasteriser is used instead.
6. **More RuneLite framework services** could be ported the same way (the
   desktop `net/runelite/client` tree has 367 files, most of them Swing-only):
   `ChatMessageManager`/chat commands, `ItemManager` (needed by item
   plugins), the account/session clients and the plugin hub.
