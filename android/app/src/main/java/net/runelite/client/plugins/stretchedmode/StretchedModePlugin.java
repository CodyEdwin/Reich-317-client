package net.runelite.client.plugins.stretchedmode;

import com.elvarg.android.ViewScaling;

import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

/**
 * Android variant of RuneLite's Stretched Mode.
 *
 * <p>The desktop plugin resizes the AWT canvas that holds the game and
 * installs mouse listeners that translate the coordinates of the stretched
 * canvas. Neither exists on Android: the client renders into a fixed
 * 765x503 framebuffer and {@code GameView} scales that framebuffer to the
 * window, translating the touch coordinates itself.
 *
 * <p>This variant therefore drives the view scaling directly
 * ({@link ViewScaling}): keep the aspect ratio or stretch to fill, use whole
 * scale factors, scale without filtering, and use a percentage of the screen.
 */
@PluginDescriptor(
        name = "Stretched Mode",
        description = "Stretches the game to fill the screen.",
        tags = {"resize", "ui", "interface", "stretch", "scaling", "fixed"},
        enabledByDefault = false
)
public class StretchedModePlugin extends Plugin {

    @Inject
    private StretchedModeConfig config;

    @Override
    protected void startUp() {
        updateConfig();
    }

    @Override
    protected void shutDown() {
        ViewScaling.setKeepAspectRatio(true);
        ViewScaling.setIntegerScaling(false);
        ViewScaling.setIncreasedPerformance(false);
        ViewScaling.setScalingFactor(100);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (StretchedModeConfig.GROUP.equals(event.getGroup())) {
            updateConfig();
        }
    }

    private void updateConfig() {
        ViewScaling.setKeepAspectRatio(config.keepAspectRatio());
        ViewScaling.setIntegerScaling(config.integerScaling());
        ViewScaling.setIncreasedPerformance(config.increasedPerformance());
        ViewScaling.setScalingFactor(config.scalingFactor());
    }
}
