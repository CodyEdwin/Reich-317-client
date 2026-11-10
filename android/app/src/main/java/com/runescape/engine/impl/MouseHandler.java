package com.runescape.engine.impl;

import com.elvarg.android.input.PointerEvent;
import com.runescape.Client;
import com.runescape.cache.graphics.Slider;
import com.runescape.engine.GameEngine;
import net.runelite.rs.api.RSMouseHandler;

/**
 * Android replacement for the desktop {@code MouseHandler}.
 *
 * <p>The desktop class is an AWT {@code MouseListener}/{@code
 * MouseMotionListener} registered on the applet; Android has no AWT, so this
 * version exposes {@link #press(int, int, boolean)}, {@link #release()} and
 * {@link #move(int, int)} that {@code GameView} calls from its touch handler.
 * The state machine - and every static field {@code Client} reads ({@code
 * mouseX}, {@code mouseY}, {@code saveClickX}, {@code saveClickY}, {@code
 * clickMode3}, {@code idleCycles}, {@code lastPressed}, {@code lastMoved}) - is
 * identical, and events still flow through {@code Client.getCallbacks()} so the
 * RuneLite mouse events fire (and can consume the event) exactly as on desktop.
 */
public class MouseHandler implements RSMouseHandler {

    public int clickType;
    public final int LEFT = 0;
    public final int RIGHT = 1;
    public final int DRAG = 2;
    public final int RELEASED = 3;
    public final int MOVE = 4;

    public static MouseHandler instance;
    public static volatile int idleCycles;
    public static volatile int currentButton;
    public static volatile long lastMoved;
    public static int mouseX;
    public static int mouseY;
    public static volatile int clickMode3;
    public static volatile int saveClickX;
    public static volatile int saveClickY;
    public static volatile long lastPressed;
    public static int lastButton;

    private int isInEvent;
    public int clickMode2;
    public int clickMode1;

    static {
        instance = new MouseHandler();
        idleCycles = 0;
        currentButton = 0;
        mouseX = -1;
        mouseY = -1;
        lastMoved = -1L;
        clickMode3 = 0;
        lastPressed = 0L;
        lastButton = 0;
        saveClickX = 0;
        saveClickY = 0;
    }

    /** The desktop focus listener cleared the button on focus loss. */
    public static void focusLost() {
        if (instance != null) {
            currentButton = 0;
        }
    }

    public static void press(int x, int y, boolean metaDown) {
        if (instance == null) {
            return;
        }
        PointerEvent mouseEvent = new PointerEvent(x, y, metaDown ? PointerEvent.RIGHT : PointerEvent.LEFT);
        if (instance.isInEvent == 0) {
            mouseEvent = Client.instance.getCallbacks().mousePressed(mouseEvent);
        }
        if (!mouseEvent.isConsumed()) {
            instance.isInEvent++;
            try {
                idleCycles = 0;
                saveClickX = mouseEvent.getX();
                saveClickY = mouseEvent.getY();
                lastPressed = GameEngine.method2692();
                if (mouseEvent.isRightButton()) {
                    instance.clickType = instance.RIGHT;
                    instance.clickMode1 = 2;
                    instance.clickMode2 = 2;
                } else {
                    instance.clickType = instance.LEFT;
                    instance.clickMode1 = 1;
                    instance.clickMode2 = 1;
                }
                Slider.handleSlider(saveClickX, saveClickY);
            } finally {
                instance.isInEvent--;
            }
        }
    }

    public static void release() {
        if (instance == null) {
            return;
        }
        idleCycles = 0;
        currentButton = 0;

        PointerEvent mouseEvent = new PointerEvent(mouseX, mouseY, PointerEvent.NONE);
        if (instance.isInEvent == 0) {
            mouseEvent = Client.instance.getCallbacks().mouseReleased(mouseEvent);
        }
        if (!mouseEvent.isConsumed()) {
            instance.isInEvent++;
            try {
                MouseWheelHandler.mouseWheelDown = false;
                instance.clickMode2 = 0;
                instance.clickType = instance.RELEASED;
            } finally {
                instance.isInEvent--;
            }
        }
    }

    public static void move(int x, int y) {
        if (instance == null) {
            return;
        }
        PointerEvent mouseEvent = new PointerEvent(x, y, PointerEvent.NONE);
        if (instance.isInEvent == 0) {
            mouseEvent = Client.instance.getCallbacks().mouseMoved(mouseEvent);
        }
        if (!mouseEvent.isConsumed()) {
            instance.isInEvent++;
            try {
                if (MouseWheelHandler.mouseWheelDown) {
                    int dy = MouseWheelHandler.mouseWheelX - mouseEvent.getX();
                    int dk = MouseWheelHandler.mouseWheelY - mouseEvent.getY();
                    Client.instance.mouseWheelDragged(dy, -dk);
                    MouseWheelHandler.mouseWheelX = mouseEvent.getX();
                    MouseWheelHandler.mouseWheelY = mouseEvent.getY();
                    return;
                }
                mouseX = mouseEvent.getX();
                mouseY = mouseEvent.getY();
                instance.clickType = instance.MOVE;
            } finally {
                instance.isInEvent--;
            }
        }
    }

    /** A press+drag while the pointer is down, mirroring {@code mouseDragged}. */
    public static void drag(int x, int y) {
        if (instance == null) {
            return;
        }
        PointerEvent mouseEvent = new PointerEvent(x, y, PointerEvent.NONE);
        if (instance.isInEvent == 0) {
            mouseEvent = Client.instance.getCallbacks().mouseDragged(mouseEvent);
        }
        if (!mouseEvent.isConsumed()) {
            instance.isInEvent++;
            try {
                if (MouseWheelHandler.mouseWheelDown) {
                    int dy = MouseWheelHandler.mouseWheelX - mouseEvent.getX();
                    int dk = MouseWheelHandler.mouseWheelY - mouseEvent.getY();
                    Client.instance.mouseWheelDragged(dy, -dk);
                    MouseWheelHandler.mouseWheelX = mouseEvent.getX();
                    MouseWheelHandler.mouseWheelY = mouseEvent.getY();
                    return;
                }
                mouseX = mouseEvent.getX();
                mouseY = mouseEvent.getY();
                instance.clickType = instance.DRAG;
                Slider.handleSlider(mouseX, mouseY);
            } finally {
                instance.isInEvent--;
            }
        }
    }

    /** Mirrors the desktop {@code mouseExited} behaviour (cursor left the canvas). */
    public static void exit() {
        if (instance != null) {
            idleCycles = 0;
            mouseX = -1;
            mouseY = -1;
            lastMoved = System.currentTimeMillis();
        }
    }
}
