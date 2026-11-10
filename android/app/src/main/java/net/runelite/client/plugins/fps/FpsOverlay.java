package net.runelite.client.plugins.fps;

import com.elvarg.android.geom.Dimension;
import com.elvarg.android.geom.Point;
import com.elvarg.android.gfx.FontMetrics;
import com.elvarg.android.gfx.Graphics2D;

import net.runelite.api.Client;
import net.runelite.api.events.FocusChanged;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;

/**
 * Android variant of the FPS indicator overlay.
 *
 * <p>The desktop class colours the text with {@code java.awt.Color} and returns
 * an AWT {@code Dimension}; here colours are ARGB ints and sizes use the
 * Android-neutral geometry types. The overlay positions itself in the top right
 * corner, so it uses {@link OverlayPosition#DYNAMIC}.
 */
public class FpsOverlay extends Overlay {

    private static final int Y_OFFSET = 1;
    private static final int X_OFFSET = 1;
    private static final String FPS_STRING = " FPS";

    private static final int COLOR_ENFORCED = 0xFFFF0000;
    private static final int COLOR_NORMAL = 0xFFFFFF00;

    private final FpsConfig config;
    private final Client client;

    /** Whether the app is in the foreground. */
    private boolean isFocused = true;

    @Inject
    public FpsOverlay(FpsConfig config, Client client) {
        this.config = config;
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.DYNAMIC);
    }

    public void onFocusChanged(FocusChanged event) {
        this.isFocused = event.isFocused();
    }

    private boolean isEnforced() {
        return config.limitFps() || (config.limitFpsUnfocused() && !isFocused);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.drawFps()) {
            return null;
        }

        final String text = client.getFPS() + FPS_STRING;
        final FontMetrics metrics = graphics.getFontMetrics();
        final int textWidth = metrics.stringWidth(text);
        final int textHeight = metrics.getAscent();

        final int width = (int) client.getRealDimensions().getWidth();
        final Point point = new Point(width - textWidth - X_OFFSET, textHeight + Y_OFFSET);

        final boolean enforced = isEnforced();
        OverlayUtil.renderTextLocation(graphics, point, text, enforced ? COLOR_ENFORCED : COLOR_NORMAL);

        return null;
    }
}
