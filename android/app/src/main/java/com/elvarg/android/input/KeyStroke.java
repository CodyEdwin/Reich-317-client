package com.elvarg.android.input;

/**
 * Android-neutral stand-in for {@code java.awt.event.KeyEvent}. The RuneLite
 * API's key callbacks are re-typed against this so plugins never touch AWT.
 *
 * <p>{@code keyCode} uses the same numeric codes as
 * {@code java.awt.event.KeyEvent} (see {@code com.elvarg.android.KeyMap}),
 * which is also what the client's own key handling expects.
 */
public final class KeyStroke {

    private final int keyCode;
    private final char keyChar;
    private boolean consumed;

    public KeyStroke(int keyCode, char keyChar) {
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }
}
