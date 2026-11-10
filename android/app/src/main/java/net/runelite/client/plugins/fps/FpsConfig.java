package net.runelite.client.plugins.fps;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/**
 * Android variant of the FPS Control configuration.
 *
 * <p>Accessors keep the desktop names; values are stored through
 * {@code ConfigBase} (see {@code ConfigBase} for why this is a class rather
 * than an interface with default methods).
 */
@ConfigGroup(FpsPlugin.CONFIG_GROUP_KEY)
public class FpsConfig extends ConfigBase {

    @ConfigItem(
            keyName = "limitFps",
            name = "Limit Global FPS",
            description = "Global FPS limit in effect regardless of whether the window is in focus",
            position = 1
    )
    public boolean limitFps() {
        return getBoolean("limitFps", false);
    }

    public void setLimitFps(boolean value) {
        set("limitFps", value);
    }

    @Range(min = 1, max = 360)
    @ConfigItem(
            keyName = "maxFps",
            name = "Global FPS target",
            description = "Desired max global frames per second",
            position = 2
    )
    public int maxFps() {
        return getInt("maxFps", 50);
    }

    public void setMaxFps(int value) {
        set("maxFps", value);
    }

    @ConfigItem(
            keyName = "limitFpsUnfocused",
            name = "Limit FPS unfocused",
            description = "FPS limit while the app is in the background",
            position = 3
    )
    public boolean limitFpsUnfocused() {
        return getBoolean("limitFpsUnfocused", false);
    }

    @Range(min = 1, max = 360)
    @ConfigItem(
            keyName = "maxFpsUnfocused",
            name = "Unfocused FPS target",
            description = "Desired max frames per second while unfocused",
            position = 4
    )
    public int maxFpsUnfocused() {
        return getInt("maxFpsUnfocused", 50);
    }

    @ConfigItem(
            keyName = "drawFps",
            name = "Draw FPS indicator",
            description = "Show the current FPS in the corner",
            position = 5
    )
    public boolean drawFps() {
        return getBoolean("drawFps", true);
    }

    public void setDrawFps(boolean value) {
        set("drawFps", value);
    }
}
