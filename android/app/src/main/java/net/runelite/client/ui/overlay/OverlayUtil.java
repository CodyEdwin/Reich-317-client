package net.runelite.client.ui.overlay;

import com.elvarg.android.geom.Point;
import com.elvarg.android.gfx.Graphics2D;

/**
 * Small drawing helpers shared by overlays.
 *
 * <p>Android variant: colours are ARGB ints (there is no {@code java.awt.Color})
 * and text is drawn with the framebuffer's {@link Graphics2D}.
 */
public final class OverlayUtil {

    private OverlayUtil() {
    }

    /** Draws text with a one pixel black shadow for readability. */
    public static void renderTextLocation(Graphics2D graphics, Point point, String text, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        graphics.setColor(0xFF000000);
        graphics.drawString(text, point.x + 1, point.y + 1);
        graphics.setColor(color);
        graphics.drawString(text, point.x, point.y);
    }

    public static void renderTextLocation(Graphics2D graphics, int x, int y, String text, int color) {
        renderTextLocation(graphics, new Point(x, y), text, color);
    }

    /** Draws outlined text (used by several HUD overlays). */
    public static void renderText(Graphics2D graphics, String text, int x, int y, int color) {
        graphics.setColor(0xFF000000);
        graphics.drawString(text, x + 1, y + 1);
        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }
}
