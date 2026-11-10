package com.runescape.engine;

import com.elvarg.android.geom.Dimension;
import com.runescape.Client;
import net.runelite.rs.api.RSCanvas;

/**
 * Android replacement for the desktop {@code com.runescape.engine.Canvas}
 * (which extends {@code java.awt.Canvas}).
 *
 * <p>Android has no AWT component model, so this is a plain value object holding
 * the current canvas size. The app's {@code GameView} owns the real view; when
 * it is resized it calls into the engine, which resizes this {@code Canvas} and
 * the raster provider.
 *
 * <p>Field and method names match the desktop class so {@code Client} is
 * unchanged, apart from the small AWT-specific spots documented in
 * {@code PORTING.md}.
 */
public final class Canvas implements RSCanvas {

    private int width;
    private int height;
    private volatile boolean focused = true;

    /** The desktop class takes the owning component; Android keeps it opaquely. */
    public Canvas(Object component) {
    }

    public Canvas() {
    }

    public void setSize(int width, int height) {
        if (Client.instance != null && Client.instance.isStretchedEnabled()) {
            Dimension stretched = Client.instance.getStretchedDimensions();
            this.width = stretched.width;
            this.height = stretched.height;
        } else {
            this.width = width;
            this.height = height;
        }
    }

    public void setLocation(int x, int y) {
        // No window to position on Android.
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /** The desktop canvas lived inside a container; Android has none. */
    public Object getParent() {
        return null;
    }

    public void setBackground(int color) {
    }

    public void setVisible(boolean visible) {
    }

    public void repaint() {
    }

    public void requestFocus() {
    }

    /** Whether {@code GameView} currently has input focus. */
    public boolean hasFocus() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
