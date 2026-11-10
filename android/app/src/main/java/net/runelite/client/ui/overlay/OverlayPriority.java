package net.runelite.client.ui.overlay;

/**
 * Render order within a layer; higher priorities draw last (on top).
 */
public enum OverlayPriority {
    LOW,
    NONE,
    MED,
    HIGH,
    HIGHEST,
}
