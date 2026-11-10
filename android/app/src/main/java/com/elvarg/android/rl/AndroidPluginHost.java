package com.elvarg.android.rl;

import com.elvarg.android.gfx.Graphics2D;

import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.entityhider.EntityHiderPlugin;
import net.runelite.client.plugins.fps.FpsPlugin;
import net.runelite.client.plugins.minimap.MinimapPlugin;
import net.runelite.client.plugins.stretchedmode.StretchedModePlugin;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayRenderer;

/**
 * Owns the RuneLite runtime on Android: config, event bus, plugin manager and
 * overlay renderer.
 *
 * <p>The desktop host ({@code net.runelite.client.RuneLite}) is a Swing
 * application: it builds a Guice injector, opens windows and runs the plugin
 * manager. Android has no such host, so this class takes its place and is
 * started lazily from the client's first tick/frame (i.e. on the game thread,
 * after {@code Client.startUp()} has finished).
 *
 * <p>Use {@link #post(Object)} to fan client events out to plugins and
 * {@link #drawOverlays(Graphics2D)} from the draw hook.
 */
public final class AndroidPluginHost {

    /**
     * Where plugin notifications are shown. The Android service registers
     * itself; the host itself does not know about the platform UI.
     */
    public interface NotificationSink {
        void show(String message);
    }

    private static volatile AndroidPluginHost instance;

    private static volatile NotificationSink notificationSink;

    /** Registers the sink that shows plugin notifications. */
    public static void setNotificationSink(NotificationSink sink) {
        notificationSink = sink;
    }

    private final ConfigManager configManager;
    private final EventBus eventBus;
    private final OverlayManager overlayManager;
    private final OverlayRenderer overlayRenderer;
    private final DrawManager drawManager;
    private final PluginManager pluginManager;
    private final ClientThread clientThread;
    private final Notifier notifier;
    private final RuneLiteConfig runeLiteConfig;
    private final ChatMessageManager chatMessageManager;
    private final ChatCommandManager chatCommandManager;

    /** Reacts to the client-wide settings. */
    private final class ConfigListener {

        @Subscribe
        public void onConfigChanged(ConfigChanged event) {
            if (RuneLiteConfig.GROUP_NAME.equals(event.getGroup())
                    && "enableNotifications".equals(event.getKey())) {
                notifier.setEnabled(runeLiteConfig.enableNotifications());
            }
        }
    }

    private AndroidPluginHost(net.runelite.api.Client client) {
        this.configManager = new ConfigManager();
        this.eventBus = new EventBus();
        this.configManager.setEventBus(eventBus);
        this.overlayManager = new OverlayManager();
        this.overlayRenderer = new OverlayRenderer(overlayManager);
        this.drawManager = new DrawManager();

        AndroidInjector injector = new AndroidInjector(configManager);
        injector.bind(net.runelite.api.Client.class, client);
        injector.bind(ConfigManager.class, configManager);
        injector.bind(EventBus.class, eventBus);
        injector.bind(OverlayManager.class, overlayManager);
        injector.bind(OverlayRenderer.class, overlayRenderer);
        injector.bind(DrawManager.class, drawManager);

        this.clientThread = new ClientThread(client);
        injector.bind(ClientThread.class, clientThread);

        this.chatMessageManager = new ChatMessageManager(client);
        injector.bind(ChatMessageManager.class, chatMessageManager);

        this.chatCommandManager = new ChatCommandManager();
        injector.bind(ChatCommandManager.class, chatCommandManager);

        this.notifier = new Notifier(eventBus);
        injector.bind(Notifier.class, notifier);
        notifier.setSink((message, type) -> {
            // Android has no tray: the notification becomes a system
            // notification, and the in-game menu keeps the history.
            NotificationSink target = notificationSink;
            if (target != null) {
                target.show(message);
            }
        });

        // The client-wide settings live in the same config group as the
        // enabled-plugin state, so they are created up front and the settings
        // menu can show them.
        this.runeLiteConfig = configManager.getConfig(RuneLiteConfig.class);
        injector.bind(RuneLiteConfig.class, runeLiteConfig);
        notifier.setEnabled(runeLiteConfig.enableNotifications());
        eventBus.register(new ConfigListener());

        this.pluginManager = new PluginManager(eventBus, configManager, injector);
        injector.bind(PluginManager.class, pluginManager);

        // Plugins shipped with the Android client. Add new (ported) plugins here.
        pluginManager.loadCorePlugins(java.util.Arrays.asList(
                FpsPlugin.class,
                EntityHiderPlugin.class,
                MinimapPlugin.class,
                StretchedModePlugin.class));
        pluginManager.startPlugins();

        registerChatCommands();
        System.out.println("[RuneLite] host started: plugins=" + pluginManager.getPlugins().size()
                + ", enabled=" + pluginManager.getEnabledPlugins().size()
                + ", overlays=" + overlayManager.getOverlays().size());
    }

    /**
     * The chat commands every client has: listing the plugins and the
     * commands. Plugins register their own with
     * {@link ChatCommandManager#registerCommand(String, ChatCommand)}.
     */
    private void registerChatCommands() {
        chatCommandManager.registerCommand("plugins", (message, args) -> {
            StringBuilder builder = new StringBuilder("Plugins: ");
            for (net.runelite.client.plugins.Plugin plugin : pluginManager.getPlugins()) {
                builder.append(plugin.getName())
                        .append(pluginManager.isPluginEnabled(plugin) ? " (on)" : " (off)")
                        .append(", ");
            }
            chatMessageManager.addMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, builder.toString());
        });

        chatCommandManager.registerCommand("commands", (message, args) -> {
            StringBuilder builder = new StringBuilder("Commands: ");
            for (String name : chatCommandManager.getCommands().keySet()) {
                builder.append("//").append(name).append(' ');
            }
            chatMessageManager.addMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, builder.toString());
        });
    }

    /** Starts the runtime if it is not running yet. Safe to call every tick. */    public static AndroidPluginHost ensureStarted() {
        AndroidPluginHost local = instance;
        if (local != null) {
            return local;
        }
        synchronized (AndroidPluginHost.class) {
            if (instance == null) {
                net.runelite.api.Client client = net.runelite.api.Client.class.cast(
                        com.runescape.Client.instance);
                if (client == null) {
                    return null;
                }
                instance = new AndroidPluginHost(client);
            }
            return instance;
        }
    }

    public static AndroidPluginHost getInstance() {
        return instance;
    }

    /** Posts an event to plugins. Dropped until the runtime has started. */
    public static void post(Object event) {
        AndroidPluginHost host = instance;
        if (host != null) {
            host.eventBus.post(event);
        }
    }

    /**
     * Runs a chat command the player typed ({@code //command}).
     *
     * @return true when a plugin handled the command, in which case the
     * message is not sent to the server
     */
    public static boolean handleChatCommand(String command) {
        AndroidPluginHost host = instance;
        return host != null && host.chatCommandManager.runCommand(command);
    }

    /**
     * Whether there is anything to paint over the frame. When false the draw
     * hook can skip creating the overlay graphics entirely (a full-screen
     * snapshot per frame is expensive on a device).
     */
    public static boolean hasOverlays() {
        AndroidPluginHost host = instance;
        return host != null && !host.overlayManager.isEmpty();
    }

    /** Draws every overlay layer over the finished frame. */
    public static void drawOverlays(Graphics2D graphics) {
        AndroidPluginHost host = instance;
        if (host != null) {
            host.overlayRenderer.renderAll(graphics);
        }
    }

    /** Called once per frame, before the frame is drawn. */
    public static void beforeRender() {
        AndroidPluginHost host = ensureStarted();
        if (host == null) {
            return;
        }
        host.eventBus.post(BeforeRender.INSTANCE);
        host.drawManager.processDrawComplete(null);
    }

    /** Called once per game tick. */
    public static void gameTick() {
        AndroidPluginHost host = ensureStarted();
        if (host != null) {
            host.clientThread.invoke();
        host.chatMessageManager.process();
        host.eventBus.post(GameTick.INSTANCE);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public OverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }

    public DrawManager getDrawManager() {
        return drawManager;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }
}
