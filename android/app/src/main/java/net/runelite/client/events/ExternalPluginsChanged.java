package net.runelite.client.events;

/**
 * Fired when the set of external plugins changes.
 *
 * <p>Android has no external plugin hub, so this is never fired; it exists so
 * plugin code that listens for it still compiles.
 */
public class ExternalPluginsChanged {

    private final boolean loaded;

    public ExternalPluginsChanged(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
