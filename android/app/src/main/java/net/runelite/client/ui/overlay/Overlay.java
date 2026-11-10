package net.runelite.client.ui.overlay;

import com.elvarg.android.geom.Dimension;
import com.elvarg.android.geom.Point;
import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.gfx.Graphics2D;

import net.runelite.client.plugins.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for RuneLite overlays on Android.
 *
 * <p>Same shape as the desktop class, with the AWT types replaced by the
 * Android-neutral ones ({@link Graphics2D}, {@link Dimension}, {@link Point},
 * {@link Rectangle}). Overlays draw into the game framebuffer through the
 * callback hook, so {@link #render(Graphics2D)} receives a graphics context
 * translated to the overlay's anchor.
 */
public abstract class Overlay {

    private final Plugin plugin;
    private Point preferredLocation;
    private Dimension preferredSize;
    private OverlayPosition preferredPosition;
    private Rectangle bounds = new Rectangle();
    private OverlayPosition position = OverlayPosition.TOP_LEFT;
    private OverlayPriority priority = OverlayPriority.NONE;
    private OverlayLayer layer = OverlayLayer.UNDER_WIDGETS;
    private boolean resizable;
    private int minimumSize = 32;
    private boolean resettable = true;
    private final List<OverlayMenuEntry> menuEntries = new ArrayList<>();

    protected Overlay() {
        this(null);
    }

    protected Overlay(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Draws the overlay.
     *
     * @return the size of the drawn content, or {@code null} if nothing was drawn
     */
    public abstract Dimension render(Graphics2D graphics);

    /** Overlay name, unique per overlay. */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * The entries of the overlay's right-click menu. The desktop client shows
     * them as menu actions; on Android the in-game menu lists them.
     */
    public List<OverlayMenuEntry> getMenuEntries() {
        return menuEntries;
    }

    public void setMenuEntries(List<OverlayMenuEntry> menuEntries) {
        this.menuEntries.clear();
        if (menuEntries != null) {
            this.menuEntries.addAll(menuEntries);
        }
    }

    /** Adds an entry to the overlay's right-click menu. */
    protected void addMenuEntry(OverlayMenuEntry entry) {
        if (entry != null) {
            menuEntries.add(entry);
        }
    }

    public void onMouseOver() {
    }

    public void revalidate() {
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Point getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(Point preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public void setPreferredSize(Dimension preferredSize) {
        this.preferredSize = preferredSize;
    }

    public OverlayPosition getPreferredPosition() {
        return preferredPosition;
    }

    public void setPreferredPosition(OverlayPosition preferredPosition) {
        this.preferredPosition = preferredPosition;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public OverlayPosition getPosition() {
        return position;
    }

    public void setPosition(OverlayPosition position) {
        this.position = position;
    }

    public OverlayPriority getPriority() {
        return priority;
    }

    public void setPriority(OverlayPriority priority) {
        this.priority = priority;
    }

    public OverlayLayer getLayer() {
        return layer;
    }

    public void setLayer(OverlayLayer layer) {
        this.layer = layer;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public int getMinimumSize() {
        return minimumSize;
    }

    public void setMinimumSize(int minimumSize) {
        this.minimumSize = minimumSize;
    }

    public boolean isResettable() {
        return resettable;
    }

    public void setResettable(boolean resettable) {
        this.resettable = resettable;
    }
}
