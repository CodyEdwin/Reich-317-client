package net.runelite.client.events;

import net.runelite.client.plugins.Plugin;

/**
 * Fired when a plugin is enabled or disabled.
 */
public class PluginChanged {

    private final Plugin plugin;

    private final boolean loaded;

    public PluginChanged(Plugin plugin, boolean loaded) {
        this.plugin = plugin;
        this.loaded = loaded;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
