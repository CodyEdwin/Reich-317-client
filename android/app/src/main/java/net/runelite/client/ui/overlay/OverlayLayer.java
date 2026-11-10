package net.runelite.client.ui.overlay;

/**
 * Render layers, mirroring RuneLite's ordering.
 */
public enum OverlayLayer {
    /** Not rendered automatically; drawn via an explicit hook. */
    MANUAL,
    /** Above the scene (actors and ground). */
    ABOVE_SCENE,
    /** Under all interfaces, above overheads. */
    UNDER_WIDGETS,
    /** Under the right-click menu. */
    ABOVE_WIDGETS,
    /** Above every game element. */
    ALWAYS_ON_TOP,
}
