package net.runelite.client.events;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

/**
 * Fired when the user picks an entry of an overlay's right-click menu.
 */
public class OverlayMenuClicked {

    private final OverlayMenuEntry entry;

    private final Overlay overlay;

    public OverlayMenuClicked(OverlayMenuEntry entry, Overlay overlay) {
        this.entry = entry;
        this.overlay = overlay;
    }

    public OverlayMenuEntry getEntry() {
        return entry;
    }

    public Overlay getOverlay() {
        return overlay;
    }
}
