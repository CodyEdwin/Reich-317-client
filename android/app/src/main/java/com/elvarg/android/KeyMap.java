package com.elvarg.android;

import android.view.KeyEvent;

/**
 * Translates Android {@link KeyEvent} codes into the AWT key codes the original
 * client expects (it was written against {@code java.awt.event.KeyEvent}).
 * Typing is carried by the unicode character, so only the special keys the
 * client maps by code - arrows, backspace, enter, function keys, and so on -
 * need an entry here.
 */
final class KeyMap {

    private KeyMap() {
    }

    static final int VK_LEFT = 37;
    static final int VK_RIGHT = 39;
    static final int VK_UP = 38;
    static final int VK_DOWN = 40;
    static final int VK_CONTROL = 17;
    static final int VK_BACK_SPACE = 8;
    static final int VK_DELETE = 127;
    static final int VK_TAB = 9;
    static final int VK_ENTER = 10;
    static final int VK_HOME = 36;
    static final int VK_END = 35;
    static final int VK_PAGE_UP = 33;
    static final int VK_PAGE_DOWN = 34;
    static final int VK_ESCAPE = 27;
    static final int VK_SHIFT = 16;
    static final int VK_SPACE = 32;

    static int toAwtKeyCode(int androidKeyCode) {
        switch (androidKeyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return VK_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return VK_RIGHT;
            case KeyEvent.KEYCODE_DPAD_UP:
                return VK_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return VK_DOWN;
            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                return VK_CONTROL;
            case KeyEvent.KEYCODE_DEL:
                return VK_BACK_SPACE;
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return VK_DELETE;
            case KeyEvent.KEYCODE_TAB:
                return VK_TAB;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                return VK_ENTER;
            case KeyEvent.KEYCODE_ESCAPE:
                return VK_ESCAPE;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return VK_SHIFT;
            case KeyEvent.KEYCODE_SPACE:
                return VK_SPACE;
            case KeyEvent.KEYCODE_MOVE_HOME:
                return VK_HOME;
            case KeyEvent.KEYCODE_MOVE_END:
                return VK_END;
            case KeyEvent.KEYCODE_PAGE_UP:
                return VK_PAGE_UP;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                return VK_PAGE_DOWN;
            case KeyEvent.KEYCODE_F1:
                return 112;
            case KeyEvent.KEYCODE_F2:
                return 113;
            case KeyEvent.KEYCODE_F3:
                return 114;
            case KeyEvent.KEYCODE_F4:
                return 115;
            case KeyEvent.KEYCODE_F5:
                return 116;
            case KeyEvent.KEYCODE_F6:
                return 117;
            case KeyEvent.KEYCODE_F7:
                return 118;
            case KeyEvent.KEYCODE_F8:
                return 119;
            case KeyEvent.KEYCODE_F9:
                return 120;
            case KeyEvent.KEYCODE_F10:
                return 121;
            case KeyEvent.KEYCODE_F11:
                return 122;
            case KeyEvent.KEYCODE_F12:
                return 123;
            default:
                return 0;
        }
    }

    /**
     * The AWT key code of a key event: the unicode character for printable
     * keys, the mapped code for the special ones.
     */
    static int toAwtKeyCode(KeyEvent event) {
        int unicode = event.getUnicodeChar();
        if (unicode > 0 && unicode < 0xFF00) {
            return Character.toUpperCase((char) unicode);
        }
        return toAwtKeyCode(event.getKeyCode());
    }

    /** Converts the Android modifier state into the AWT-style modifier mask. */
    static int toAwtModifiers(int metaState) {
        int modifiers = 0;
        if ((metaState & KeyEvent.META_CTRL_ON) != 0) {
            modifiers |= net.runelite.client.config.Keybind.CTRL_DOWN_MASK;
        }
        if ((metaState & KeyEvent.META_ALT_ON) != 0) {
            modifiers |= net.runelite.client.config.Keybind.ALT_DOWN_MASK;
        }
        if ((metaState & KeyEvent.META_SHIFT_ON) != 0) {
            modifiers |= net.runelite.client.config.Keybind.SHIFT_DOWN_MASK;
        }
        if ((metaState & KeyEvent.META_META_ON) != 0) {
            modifiers |= net.runelite.client.config.Keybind.META_DOWN_MASK;
        }
        return modifiers;
    }
}
