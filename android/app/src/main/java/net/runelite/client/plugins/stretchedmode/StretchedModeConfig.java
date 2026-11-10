package net.runelite.client.plugins.stretchedmode;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

/**
 * Android variant of RuneLite's Stretched Mode configuration.
 *
 * <p>The keys match the desktop plugin. The defaults differ: on Android the
 * game keeps its aspect ratio (letterboxed) and uses the whole screen, which
 * is the sane default on a phone.
 */
@ConfigGroup(StretchedModeConfig.GROUP)
public class StretchedModeConfig extends ConfigBase {

    public static final String GROUP = "stretchedmode";

    @ConfigItem(
            keyName = "keepAspectRatio",
            name = "Keep aspect ratio",
            description = "Keeps the aspect ratio when stretching."
    )
    public boolean keepAspectRatio() {
        return getBoolean("keepAspectRatio", true);
    }

    public void setKeepAspectRatio(boolean value) {
        set("keepAspectRatio", value);
    }

    @ConfigItem(
            keyName = "increasedPerformance",
            name = "Increased performance mode",
            description = "Scales the game without filtering, which is faster but blockier."
    )
    public boolean increasedPerformance() {
        return getBoolean("increasedPerformance", false);
    }

    public void setIncreasedPerformance(boolean value) {
        set("increasedPerformance", value);
    }

    @ConfigItem(
            keyName = "integerScaling",
            name = "Integer Scaling",
            description = "Forces use of a whole number scale factor when stretching."
    )
    public boolean integerScaling() {
        return getBoolean("integerScaling", false);
    }

    public void setIntegerScaling(boolean value) {
        set("integerScaling", value);
    }

    @ConfigItem(
            keyName = "scalingFactor",
            name = "Resizable Scaling",
            description = "How much of the screen the game uses before it is stretched."
    )
    @Units(Units.PERCENT)
    @Range(min = 10, max = 100)
    public int scalingFactor() {
        return getInt("scalingFactor", 100);
    }

    public void setScalingFactor(int value) {
        set("scalingFactor", value);
    }
}
