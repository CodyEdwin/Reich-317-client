package com.elvarg.android.input;

/**
 * Android-neutral stand-in for {@code java.awt.event.MouseEvent} /
 * {@code MouseWheelEvent}. The RuneLite API's mouse callbacks are re-typed
 * against this so plugins never touch AWT.
 */
public final class PointerEvent {

    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int MIDDLE = 2;
    public static final int RIGHT = 3;

    private final int x;
    private final int y;
    private final int button;
    private final int wheelRotation;
    private final long when;
    private boolean consumed;
    private boolean popupTrigger;

    public PointerEvent(int x, int y, int button) {
        this(x, y, button, 0, System.currentTimeMillis());
    }

    public PointerEvent(int x, int y, int button, int wheelRotation, long when) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.wheelRotation = wheelRotation;
        this.when = when;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getButton() {
        return button;
    }

    public int getWheelRotation() {
        return wheelRotation;
    }

    public long getWhen() {
        return when;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

    public boolean isPopupTrigger() {
        return popupTrigger;
    }

    public void setPopupTrigger(boolean popupTrigger) {
        this.popupTrigger = popupTrigger;
    }

    public boolean isLeftButton() {
        return button == LEFT;
    }

    public boolean isRightButton() {
        return button == RIGHT;
    }

    public boolean isMiddleButton() {
        return button == MIDDLE;
    }
}
