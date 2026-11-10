package net.runelite.client.plugins;

import com.elvarg.android.rl.AndroidInjector;
import com.google.inject.Injector;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginChanged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Android implementation of RuneLite's plugin manager.
 *
 * <p>Differences from the desktop version:
 * <ul>
 *   <li>Plugins are created with {@link AndroidInjector} instead of a Guice
 *       child injector.</li>
 *   <li>There is no plugin hub / external class loading on Android, so
 *       {@code loadCorePlugins} takes the classes the app ships with.</li>
 *   <li>Enabled state is stored in {@link ConfigManager} under the
 *       {@code runelite} group as {@code plugin.<SimpleName>}.</li>
 * </ul>
 */
public final class PluginManager {

    public static final String CONFIG_GROUP = "runelite";

    private final EventBus eventBus;
    private final ConfigManager configManager;
    private final AndroidInjector injector;

    private final Map<Class<? extends Plugin>, Plugin> plugins = new LinkedHashMap<>();
    private final Set<Plugin> enabled = new LinkedHashSet<>();
    private boolean started;

    public PluginManager(EventBus eventBus, ConfigManager configManager, AndroidInjector injector) {
        this.eventBus = eventBus;
        this.configManager = configManager;
        this.injector = injector;
    }

    /**
     * Instantiates the given plugin classes and remembers which of them are
     * enabled. Does not start anything until {@link #startPlugins()}.
     */
    public synchronized void loadCorePlugins(Collection<Class<? extends Plugin>> classes) {
        for (Class<? extends Plugin> type : classes) {
            try {
                Plugin plugin = injector.getInstance(type);
                plugins.put(type, plugin);
                if (isPluginEnabled(plugin)) {
                    enabled.add(plugin);
                }
            } catch (RuntimeException e) {
                System.err.println("PluginManager: could not load " + type.getName());
                e.printStackTrace();
            }
        }
    }

    /** Starts every enabled plugin and subscribes it to the event bus. */
    public synchronized void startPlugins() {
        if (started) {
            return;
        }
        started = true;
        for (Plugin plugin : new ArrayList<>(enabled)) {
            startPlugin(plugin);
        }
    }

    /** Stops all running plugins. */
    public synchronized void stopPlugins() {
        for (Plugin plugin : new ArrayList<>(enabled)) {
            stopPlugin(plugin);
        }
        started = false;
    }

    public synchronized boolean isPluginEnabled(Plugin plugin) {
        PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
        boolean defaultValue = descriptor == null || descriptor.enabledByDefault();
        return configManager.getBoolean(CONFIG_GROUP, configKey(plugin), defaultValue);
    }

    public synchronized void setPluginEnabled(Plugin plugin, boolean enable) {
        configManager.setConfiguration(CONFIG_GROUP, configKey(plugin), Boolean.toString(enable));
        if (enable) {
            enabled.add(plugin);
            if (started) {
                startPlugin(plugin);
            }
        } else {
            if (started) {
                stopPlugin(plugin);
            }
            enabled.remove(plugin);
        }
        eventBus.post(new PluginChanged(plugin, enable));
    }

    public synchronized List<Plugin> getPlugins() {
        return Collections.unmodifiableList(new ArrayList<>(plugins.values()));
    }

    public synchronized List<Plugin> getEnabledPlugins() {
        return Collections.unmodifiableList(new ArrayList<>(enabled));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Injector getInjector() {
        return null;
    }

    private void startPlugin(Plugin plugin) {
        try {
            plugin.startUp();
        } catch (Exception e) {
            System.err.println("PluginManager: " + plugin.getName() + " failed to start");
            e.printStackTrace();
            return;
        }
        eventBus.register(plugin);
    }

    private void stopPlugin(Plugin plugin) {
        eventBus.unregister(plugin);
        try {
            plugin.shutDown();
        } catch (Exception e) {
            System.err.println("PluginManager: " + plugin.getName() + " failed to stop");
            e.printStackTrace();
        }
    }

    private static String configKey(Plugin plugin) {
        return "plugin." + plugin.getClass().getSimpleName();
    }
}
