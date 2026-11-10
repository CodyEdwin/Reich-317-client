package net.runelite.api.events;

/**
 * Fired when the mouse wheel is turned.
 *
 * <p>Android has no AWT {@code MouseWheelEvent}: the touch layer turns a
 * two-finger vertical drag into wheel rotations and posts this event so
 * plugins can react to them the way the desktop {@code MouseManager} did.
 * The desktop client's own wheel handling is unchanged; this event is
 * additional.
 */
public class MouseWheelMoved {

    private final int rotation;

    public MouseWheelMoved(int rotation) {
        this.rotation = rotation;
    }

    /**
     * The number of "clicks" the wheel was turned; positive is away from the
     * user (scrolling down).
     */
    public int getRotation() {
        return rotation;
    }
}
