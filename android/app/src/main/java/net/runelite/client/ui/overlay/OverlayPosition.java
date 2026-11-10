package net.runelite.client.ui.overlay;

/**
 * Where an overlay is anchored on the canvas.
 */
public enum OverlayPosition {
    /** Not attached anywhere, but still movable. */
    DETACHED,
    /** The overlay places itself where it wants (uses its own coordinates). */
    DYNAMIC,
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    ABOVE_CHATBOX_RIGHT,
    CANVAS_TOP_RIGHT,
    TOOLTIP,
}
