package net.runelite.rs.api;

import net.runelite.api.KeyFocusListener;

/**
 * Android port of {@code RSKeyHandler}. The desktop interface mixes in the AWT
 * {@link java.awt.event.KeyListener}/{@code FocusListener} interfaces; Android
 * has neither, so only the RuneLite marker interface remains. The Android
 * {@code KeyHandler} is fed input directly by the app instead.
 */
public interface RSKeyHandler extends KeyFocusListener {
}
