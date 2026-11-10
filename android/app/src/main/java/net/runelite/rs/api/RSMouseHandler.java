package net.runelite.rs.api;

/**
 * Android port of {@code RSMouseHandler}. The desktop interface extends the AWT
 * {@code MouseListener}/{@code MouseMotionListener}/{@code FocusListener}
 * interfaces; Android has none of those, so this is just a marker interface.
 * The Android {@code MouseHandler} is driven directly by {@code GameView}.
 */
public interface RSMouseHandler {
}
