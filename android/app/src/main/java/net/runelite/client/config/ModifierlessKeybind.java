package net.runelite.client.config;

/**
 * A keybind that ignores the modifier keys.
 */
public class ModifierlessKeybind extends Keybind {

    public ModifierlessKeybind(int keyCode, int modifiers) {
        super(keyCode, modifiers, true);
    }

    @Override
    public boolean matches(int keyCode, int modifiers) {
        return matches(keyCode, modifiers, true);
    }
}
