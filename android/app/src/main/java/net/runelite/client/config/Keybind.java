package net.runelite.client.config;

import com.elvarg.android.input.KeyCode;

import java.util.HashMap;
import java.util.Map;

/**
 * A combination of zero or more modifier keys (Ctrl, alt, shift, meta) and an
 * optional non-modifier key.
 *
 * <p>Java port of the desktop class: AWT is not available on Android, so the
 * modifier masks and the key codes are the (identical) numeric constants and
 * {@link KeyCode} is used instead of {@code java.awt.event.KeyEvent}.
 */
public class Keybind {

    // The same values as java.awt.event.InputEvent.
    public static final int SHIFT_DOWN_MASK = 1 << 6;
    public static final int CTRL_DOWN_MASK = 1 << 7;
    public static final int META_DOWN_MASK = 1 << 8;
    public static final int ALT_DOWN_MASK = 1 << 9;

    private static final Map<Integer, Integer> MODIFIER_TO_KEY_CODE = new HashMap<>();

    static {
        MODIFIER_TO_KEY_CODE.put(CTRL_DOWN_MASK, KeyCode.VK_CONTROL);
        MODIFIER_TO_KEY_CODE.put(ALT_DOWN_MASK, KeyCode.VK_ALT);
        MODIFIER_TO_KEY_CODE.put(SHIFT_DOWN_MASK, KeyCode.VK_SHIFT);
        MODIFIER_TO_KEY_CODE.put(META_DOWN_MASK, KeyCode.VK_META);
    }

    // Bitmask of all supported modifiers
    private static final int KEYBOARD_MODIFIER_MASK = MODIFIER_TO_KEY_CODE.keySet().stream()
            .reduce((a, b) -> a | b).get();

    public static final Keybind NOT_SET = new Keybind(KeyCode.VK_UNDEFINED, 0);

    public static final Keybind CTRL = new Keybind(KeyCode.VK_UNDEFINED, CTRL_DOWN_MASK);
    public static final Keybind ALT = new Keybind(KeyCode.VK_UNDEFINED, ALT_DOWN_MASK);
    public static final Keybind SHIFT = new Keybind(KeyCode.VK_UNDEFINED, SHIFT_DOWN_MASK);

    private final int keyCode;

    private final int modifiers;

    protected Keybind(int keyCode, int modifiers, boolean ignoreModifiers) {
        modifiers &= KEYBOARD_MODIFIER_MASK;

        // If the keybind is just modifiers we don't want the keyCode to contain
        // the modifier too, because this breaks if you do the keycode backwards.
        Integer mf = getModifierForKeyCode(keyCode);
        if (mf != null) {
            modifiers |= mf;
            keyCode = KeyCode.VK_UNDEFINED;
        }

        if (ignoreModifiers && keyCode != KeyCode.VK_UNDEFINED) {
            modifiers = 0;
        }

        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public Keybind(int keyCode, int modifiers) {
        this(keyCode, modifiers, false);
    }

    /**
     * Whether the given key press matches this hotkey. The key code and the
     * modifier mask use the AWT values the client works with.
     */
    public boolean matches(int keyCode, int modifiers) {
        return matches(keyCode, modifiers, false);
    }

    protected boolean matches(int keyCode, int modifiers, boolean ignoreModifiers) {
        if (NOT_SET.equals(this)) {
            return false;
        }

        int pressedModifiers = modifiers & KEYBOARD_MODIFIER_MASK;

        Integer mf = getModifierForKeyCode(keyCode);
        if (mf != null) {
            pressedModifiers |= mf;
            keyCode = KeyCode.VK_UNDEFINED;
        }

        if (ignoreModifiers && keyCode != KeyCode.VK_UNDEFINED) {
            return this.keyCode == keyCode;
        }

        return this.keyCode == keyCode && this.modifiers == pressedModifiers;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        if (keyCode == KeyCode.VK_UNDEFINED && modifiers == 0) {
            return "Not set";
        }

        String key = keyCode == KeyCode.VK_UNDEFINED ? "" : KeyCode.getKeyText(keyCode);

        String mod = "";
        if (modifiers != 0) {
            StringBuilder builder = new StringBuilder();
            if ((modifiers & CTRL_DOWN_MASK) != 0) {
                builder.append("Ctrl");
            }
            if ((modifiers & ALT_DOWN_MASK) != 0) {
                append(builder, "Alt");
            }
            if ((modifiers & SHIFT_DOWN_MASK) != 0) {
                append(builder, "Shift");
            }
            if ((modifiers & META_DOWN_MASK) != 0) {
                append(builder, "Meta");
            }
            mod = builder.toString();
        }

        if (mod.isEmpty() && key.isEmpty()) {
            return "Not set";
        }
        if (!mod.isEmpty() && !key.isEmpty()) {
            return mod + "+" + key;
        }
        if (mod.isEmpty()) {
            return key;
        }
        return mod;
    }

    private static void append(StringBuilder builder, String text) {
        if (builder.length() > 0) {
            builder.append('+');
        }
        builder.append(text);
    }

    public static Integer getModifierForKeyCode(int keyCode) {
        for (Map.Entry<Integer, Integer> entry : MODIFIER_TO_KEY_CODE.entrySet()) {
            if (entry.getValue() == keyCode) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Keybind)) {
            return false;
        }
        Keybind other = (Keybind) o;
        return keyCode == other.keyCode && modifiers == other.modifiers;
    }

    @Override
    public int hashCode() {
        return keyCode * 31 + modifiers;
    }
}
