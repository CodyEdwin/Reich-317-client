package net.runelite.client.ui.overlay;

import com.elvarg.android.geom.Dimension;
import com.elvarg.android.geom.Point;
import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.gfx.Graphics2D;
import com.runescape.engine.GameEngine;

import java.util.List;

/**
 * Draws registered overlays into the frame.
 *
 * <p>RuneLite measures each overlay by rendering it into an offscreen image, so
 * it can anchor overlays to the right/bottom edges before drawing them. The
 * Android port draws directly into the framebuffer, so an overlay either
 * positions itself ({@link OverlayPosition#DYNAMIC}, the common case for
 * text/HUD overlays) or declares its size with
 * {@link Overlay#setPreferredSize(Dimension)}. Anchors without a known size fall
 * back to the top left rather than drawing off-screen.
 */
public final class OverlayRenderer {

    private final OverlayManager overlayManager;

    public OverlayRenderer(OverlayManager overlayManager) {
        this.overlayManager = overlayManager;
    }

    /** Renders one layer; call once per layer in back-to-front order. */
    public void renderOverlayLayer(Graphics2D graphics, OverlayLayer layer) {
        List<Overlay> overlays = overlayManager.getOverlays(layer);
        for (Overlay overlay : overlays) {
            Point location = resolveLocation(overlay);
            boolean translated = location.x != 0 || location.y != 0;
            if (translated) {
                graphics.translate(location.x, location.y);
            }
            try {
                Dimension size = overlay.render(graphics);
                if (size != null) {
                    // Remember where the overlay was drawn so a right click
                    // can be routed to it.
                    overlay.setBounds(new Rectangle(location.x, location.y, size.width, size.height));
                }
            } catch (Throwable t) {
                System.err.println("OverlayRenderer: " + overlay.getName() + " failed to render");
                t.printStackTrace();
            } finally {
                if (translated) {
                    graphics.translate(-location.x, -location.y);
                }
            }
        }
    }

    /** Renders every layer the client draws automatically. */
    public void renderAll(Graphics2D graphics) {
        renderOverlayLayer(graphics, OverlayLayer.UNDER_WIDGETS);
        renderOverlayLayer(graphics, OverlayLayer.ABOVE_WIDGETS);
        renderOverlayLayer(graphics, OverlayLayer.ALWAYS_ON_TOP);
    }

    private static Point resolveLocation(Overlay overlay) {
        Point location = overlay.getPreferredLocation();
        if (location != null) {
            return location;
        }

        Dimension size = overlay.getPreferredSize();
        if (size == null) {
            // The overlay positions its own content (typical for DYNAMIC).
            return new Point(0, 0);
        }

        int canvasWidth = GameEngine.canvasWidth;
        int canvasHeight = GameEngine.canvasHeight;

        switch (overlay.getPosition()) {
            case TOP_RIGHT:
            case CANVAS_TOP_RIGHT:
            case ABOVE_CHATBOX_RIGHT:
                return new Point(canvasWidth - size.width, 0);
            case TOP_CENTER:
                return new Point((canvasWidth - size.width) / 2, 0);
            case BOTTOM_LEFT:
                return new Point(0, canvasHeight - size.height);
            case BOTTOM_RIGHT:
                return new Point(canvasWidth - size.width, canvasHeight - size.height);
            default:
                return new Point(0, 0);
        }
    }
}
