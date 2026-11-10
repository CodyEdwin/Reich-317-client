package com.runescape.model.content;

import com.runescape.Client;
import com.runescape.Configuration;
import com.runescape.UserPreferences;
import com.runescape.cache.graphics.widget.Widget;

import com.elvarg.android.input.KeyCode;

/**
 * Handles tab keybinds
 *
 * @author Ghost
 */
public class Keybinding {

    public static final int MIN_FRAME = 53009;
    public static final int RESTORE_DEFAULT = 53004;
    public static final int ESCAPE_CONFIG = 53006;
    public static final String[] OPTIONS = {"None", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"};

    public static final int KEYS[] = {-1, KeyCode.VK_F1, KeyCode.VK_F2, KeyCode.VK_F3, KeyCode.VK_F4, KeyCode.VK_F5, KeyCode.VK_F6, KeyCode.VK_F7, KeyCode.VK_F8,
            KeyCode.VK_F9, KeyCode.VK_F10, KeyCode.VK_F11, KeyCode.VK_F12};


    public static int[] KEYBINDINGS;

    static {
        restoreDefault();
    }

    public static void restoreDefault() {
        KEYBINDINGS = new int[]{
                KeyCode.VK_F5,
                -1,
                -1,
                KeyCode.VK_F1,
                KeyCode.VK_F2,
                KeyCode.VK_F3,
                KeyCode.VK_F4,
                KeyCode.VK_F6,
                KeyCode.VK_F7,
                KeyCode.VK_F8,
                KeyCode.VK_F9,
                KeyCode.VK_F10,
                KeyCode.VK_F11,
                KeyCode.VK_F12,
        };
        Client.preferences.setEscapeCloseInterface(true);
    }

    public static void checkDuplicates(int key, int index) {
        for (int i = 0; i < KEYBINDINGS.length; i++) {
            if (KEYS[key] == KEYBINDINGS[i] && i != index && KEYBINDINGS[i] != -1) {
                KEYBINDINGS[i] = -1;
                Widget.interfaceCache[MIN_FRAME + 3 * i].dropdown.setSelected("None");
            }
        }
        if (index != -1 && KEYS[key] == KeyCode.VK_ESCAPE && Client.preferences.getEscapeCloseInterface()) {
            Client.preferences.setEscapeCloseInterface(!Client.preferences.getEscapeCloseInterface());
            Widget.interfaceCache[ESCAPE_CONFIG].active = Client.preferences.getEscapeCloseInterface();
        }
    }

    public static void bind(int index, int key) {
        checkDuplicates(key, index);
        KEYBINDINGS[index] = KEYS[key];
        UserPreferences.INSTANCE.save();
    }

    public static boolean isBound(int key) {
        for (int i = 0; i < KEYBINDINGS.length; i++) {
            if (key == KEYBINDINGS[i]) {
                Client.setTab(i);
                return true;
            }
        }
        return false;
    }

    public static void updateInterface() {

        for (int i = 0; i < OPTIONS.length; i++) {

            int key = KEYBINDINGS[i];
            String current = "None";

            if (key == -1) {
                // None
            } else {
                // F1 - F12: 112 - 123
                current = OPTIONS[key - 111];
            }

            Widget.interfaceCache[MIN_FRAME + 3 * i].dropdown.setSelected(current);
        }
        Widget.interfaceCache[ESCAPE_CONFIG].active = Client.preferences.getEscapeCloseInterface();
    }
}
