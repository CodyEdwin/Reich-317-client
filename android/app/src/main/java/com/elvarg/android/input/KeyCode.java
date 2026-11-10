package com.elvarg.android.input;

/**
 * Android-neutral replacement for the {@code VK_*} constants and static helpers
 * of {@code java.awt.event.KeyEvent}.
 *
 * <p>The client was written against AWT key codes, and its key handling (chat,
 * dialog shortcuts, keybinds, teleport menu) is all expressed in terms of those
 * constants and of {@link #getKeyText(int)} / {@link #getExtendedKeyCodeForChar(char)}.
 * The numeric values here are identical to AWT's, and
 * {@link com.elvarg.android.KeyMap} maps Android key codes onto them.
 */
public final class KeyCode {

    private KeyCode() {
    }

    // Control / editing keys
    public static final int VK_UNDEFINED = 0;
    public static final int VK_BACK_SPACE = 8;
    public static final int VK_TAB = 9;
    public static final int VK_ENTER = 10;
    public static final int VK_SHIFT = 16;
    public static final int VK_CONTROL = 17;
    public static final int VK_ALT = 18;
    public static final int VK_META = 91;
    public static final int VK_ESCAPE = 27;
    public static final int VK_SPACE = 32;
    public static final int VK_PAGE_UP = 33;
    public static final int VK_PAGE_DOWN = 34;
    public static final int VK_END = 35;
    public static final int VK_HOME = 36;
    public static final int VK_LEFT = 37;
    public static final int VK_UP = 38;
    public static final int VK_RIGHT = 39;
    public static final int VK_DOWN = 40;
    public static final int VK_DELETE = 127;

    // Function keys
    public static final int VK_F1 = 112;
    public static final int VK_F2 = 113;
    public static final int VK_F3 = 114;
    public static final int VK_F4 = 115;
    public static final int VK_F5 = 116;
    public static final int VK_F6 = 117;
    public static final int VK_F7 = 118;
    public static final int VK_F8 = 119;
    public static final int VK_F9 = 120;
    public static final int VK_F10 = 121;
    public static final int VK_F11 = 122;
    public static final int VK_F12 = 123;

    // Letters (== AWT's VK_A .. VK_Z)
    public static final int VK_A = 65;
    public static final int VK_B = 66;
    public static final int VK_C = 67;
    public static final int VK_D = 68;
    public static final int VK_E = 69;
    public static final int VK_F = 70;
    public static final int VK_G = 71;
    public static final int VK_H = 72;
    public static final int VK_I = 73;
    public static final int VK_J = 74;
    public static final int VK_K = 75;
    public static final int VK_L = 76;
    public static final int VK_M = 77;
    public static final int VK_N = 78;
    public static final int VK_O = 79;
    public static final int VK_P = 80;
    public static final int VK_Q = 81;
    public static final int VK_R = 82;
    public static final int VK_S = 83;
    public static final int VK_T = 84;
    public static final int VK_U = 85;
    public static final int VK_V = 86;
    public static final int VK_W = 87;
    public static final int VK_X = 88;
    public static final int VK_Y = 89;
    public static final int VK_Z = 90;

    public static final int VK_0 = 48;
    public static final int VK_1 = 49;
    public static final int VK_2 = 50;
    public static final int VK_3 = 51;
    public static final int VK_4 = 52;
    public static final int VK_5 = 53;
    public static final int VK_6 = 54;
    public static final int VK_7 = 55;
    public static final int VK_8 = 56;
    public static final int VK_9 = 57;

    private static final String[] NAMES = new String[256];

    static {
        NAMES[VK_BACK_SPACE] = "Backspace";
        NAMES[VK_TAB] = "Tab";
        NAMES[VK_ENTER] = "Enter";
        NAMES[VK_SHIFT] = "Shift";
        NAMES[VK_CONTROL] = "Ctrl";
        NAMES[VK_ALT] = "Alt";
        NAMES[VK_META] = "Meta";
        NAMES[VK_ESCAPE] = "Escape";
        NAMES[VK_SPACE] = "Space";
        NAMES[VK_PAGE_UP] = "Page Up";
        NAMES[VK_PAGE_DOWN] = "Page Down";
        NAMES[VK_END] = "End";
        NAMES[VK_HOME] = "Home";
        NAMES[VK_LEFT] = "Left";
        NAMES[VK_UP] = "Up";
        NAMES[VK_RIGHT] = "Right";
        NAMES[VK_DOWN] = "Down";
        NAMES[VK_DELETE] = "Delete";
        for (int i = 0; i < 12; i++) {
            NAMES[VK_F1 + i] = "F" + (i + 1);
        }
        for (int c = 'A'; c <= 'Z'; c++) {
            NAMES[c] = String.valueOf((char) c);
        }
        for (int c = '0'; c <= '9'; c++) {
            NAMES[c] = String.valueOf((char) c);
        }
    }

    /** Mirrors {@code java.awt.event.KeyEvent.getKeyText(int)}. */
    public static String getKeyText(int keyCode) {
        if (keyCode >= 0 && keyCode < NAMES.length && NAMES[keyCode] != null) {
            return NAMES[keyCode];
        }
        return "Unknown keyCode: " + keyCode;
    }

    /**
     * Mirrors {@code java.awt.event.KeyEvent.getExtendedKeyCodeForChar(int)}
     * (the AWT method takes an {@code int}, not a {@code char}) for the
     * characters the client's teleport menu cares about (letters and digits).
     * Returns {@code VK_UNDEFINED} (0) for anything else.
     */
    public static int getExtendedKeyCodeForChar(int c) {
        if (c >= 'a' && c <= 'z') {
            return c - 32;
        }
        if (c >= 'A' && c <= 'Z') {
            return c;
        }
        if (c >= '0' && c <= '9') {
            return c;
        }
        return 0;
    }
}
