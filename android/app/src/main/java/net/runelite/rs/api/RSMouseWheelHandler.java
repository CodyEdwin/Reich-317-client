package net.runelite.rs.api;

/**
 * Android port of {@code RSMouseWheelHandler}. The desktop interface extends
 * the AWT {@code MouseWheelListener}; Android has no AWT, so this is just a
 * marker interface. Wheel input is delivered to the Android
 * {@code MouseWheelHandler} directly from {@code GameView}.
 */
public interface RSMouseWheelHandler {
}
