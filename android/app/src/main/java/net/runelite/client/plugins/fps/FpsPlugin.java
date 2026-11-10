package net.runelite.client.plugins.fps;

import net.runelite.api.events.FocusChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

/**
 * Android variant of the FPS Control plugin.
 *
 * <p>Same features as the desktop plugin: the frame-rate indicator and the
 * frame-rate limiter. The limiter is a draw listener that sleeps for the
 * remainder of the frame time, exactly like on desktop.
 */
@PluginDescriptor(
        name = "FPS Control",
        description = "Show current FPS and/or set an FPS limit",
        tags = {"frames", "framerate", "limit", "overlay"},
        enabledByDefault = true
)
public class FpsPlugin extends Plugin {

    public static final String CONFIG_GROUP_KEY = "fpscontrol";

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private FpsOverlay overlay;

    @Inject
    private FpsDrawListener drawListener;

    @Inject
    private DrawManager drawManager;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        drawManager.registerEveryFrameListener(drawListener);
        drawListener.reloadConfig();
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        drawManager.unregisterEveryFrameListener(drawListener);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (CONFIG_GROUP_KEY.equals(event.getGroup())) {
            drawListener.reloadConfig();
        }
    }

    @Subscribe
    public void onFocusChanged(FocusChanged event) {
        drawListener.onFocusChanged(event);
        overlay.onFocusChanged(event);
    }
}
