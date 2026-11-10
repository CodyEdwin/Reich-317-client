package com.runescape.engine.impl;

import com.elvarg.android.input.KeyCode;
import com.elvarg.android.input.KeyStroke;
import com.runescape.Client;
import com.runescape.UserPreferences;
import com.runescape.draw.Console;
import com.runescape.loginscreen.LoginBackground;
import com.runescape.model.content.Keybinding;
import net.runelite.api.GameState;
import net.runelite.rs.api.RSKeyHandler;

/**
 * Android replacement for the desktop {@code KeyHandler}.
 *
 * <p>The desktop class is an AWT {@code KeyListener}/{@code FocusListener};
 * Android delivers key input through {@link #press(int, char)} / {@link
 * #release(int, char)} called by {@code GameView}. Key codes are the AWT-style
 * constants in {@link KeyCode}, which {@link com.elvarg.android.KeyMap} maps
 * from Android's codes. Everything else - the {@code keyArray}, the character
 * queue read by {@code Client}, keybinds, dialogue skip keys, camera toggles - is
 * unchanged, and events still flow through {@code Client.getCallbacks()}.
 */
public final class KeyHandler implements RSKeyHandler {

    public static KeyHandler instance;

    static {
        instance = new KeyHandler();
    }

    public final int[] keyArray = new int[128];
    private final int[] charQueue = new int[128];
    private int readIndex;
    private int writeIndex;
    int keyPressed = 0;
    public static volatile int idleCycles = 0;

    /** The desktop focus listener zeroed the held-key state on focus loss. */
    public static void focusLost() {
        for (int i = 0; i < instance.keyArray.length; i++) {
            instance.keyArray[i] = 0;
        }
    }

    public static void press(int keyCode, char keyChar) {
        Client.instance.getCallbacks().keyPressed(new KeyStroke(keyCode, keyChar));
        idleCycles = 0;
        instance.handlePress(keyCode, keyChar);
    }

    private void handlePress(int i, int keyChar) {
        if (i == KeyCode.VK_F1 && Client.instance.gameState == GameState.LOGIN_SCREEN.getState()) {
            if (Client.preferences.getLoginBackground() == LoginBackground.FADING_BACKGROUNDS) {
                Client.preferences.setLoginBackground(LoginBackground.NORMAL);
            } else {
                Client.preferences.setLoginBackground(LoginBackground.FADING_BACKGROUNDS);
            }
            Client.instance.cinematicScene.toggle(false);
        }

        if (i == KeyCode.VK_F2 && Client.instance.gameState == GameState.LOGIN_SCREEN.getState()) {
            if (Client.preferences.getLoginBackground() != LoginBackground.ANIMATED_GAME_WORLD) {
                System.out.println("ANIMATED");
                Client.preferences.setLoginBackground(LoginBackground.ANIMATED_GAME_WORLD);
                Client.instance.cinematicScene.toggle(true);
            } else {
                Client.preferences.setLoginBackground(LoginBackground.FADING_BACKGROUNDS);
                Client.instance.cinematicScene.toggle(false);
            }
            UserPreferences.INSTANCE.save();
        }

        if (i == KeyCode.VK_SHIFT) {
            keyPressed = KeyCode.VK_SHIFT;
            Client.shiftDown = true;
        } else {
            keyPressed = i;
        }

        if (dialogueSkipKeys(i)) {
            return;
        }

        if (Keybinding.isBound(i)) {
            return;
        }

        if (i == KeyCode.VK_ESCAPE && Client.preferences.getEscapeCloseInterface()) {
            if (Client.loggedIn && Client.openInterfaceId != -1) {
                Client.instance.packetSender.sendInterfaceClear();
                return;
            }
        }

        if (keyChar < 30)
            keyChar = 0;
        if (i == 37)
            keyChar = 1;
        if (i == 39)
            keyChar = 2;
        if (i == 38)
            keyChar = 3;
        if (i == 40)
            keyChar = 4;
        if (i == 17)
            keyChar = 5;
        if (i == 8)
            keyChar = 8;
        if (i == 127)
            keyChar = 8;
        if (i == 9)
            keyChar = 9;
        if (i == 10)
            keyChar = 10;
        if (i >= 112 && i <= 123)
            keyChar = (1008 + i) - 112;
        if (i == 36)
            keyChar = 1000;
        if (i == 35)
            keyChar = 1001;
        if (i == 33)
            keyChar = 1002;
        if (i == 34)
            keyChar = 1003;
        if (keyChar > 0 && keyChar < 128)
            keyArray[keyChar] = 1;
        if (Client.instance.getOculusOrbState() == 0 && keyChar > 4) {
            charQueue[writeIndex] = keyChar;
            writeIndex = writeIndex + 1 & 0x7f;
        }
    }

    public int readChar() {
        int k = -1;
        if (writeIndex != readIndex) {
            k = charQueue[readIndex];
            readIndex = readIndex + 1 & 0x7f;
        }
        return k;
    }

    public static void release(int keyCode, char keyChar) {
        Client.instance.getCallbacks().keyReleased(new KeyStroke(keyCode, keyChar));
        idleCycles = 0;
        instance.handleRelease(keyCode, keyChar);
    }

    private void handleRelease(int i, char c) {
        if (i == KeyCode.VK_SHIFT) {
            Client.shiftDown = false;
        }

        if (c < '\036')
            c = '\0';
        if (i == 37)
            c = '\001';
        if (i == 39)
            c = '\002';
        if (i == 38)
            c = '\003';
        if (i == 40)
            c = '\004';
        if (i == 17)
            c = '\005';
        if (i == 8)
            c = '\b';
        if (i == 127)
            c = '\b';
        if (i == 9)
            c = '\t';
        if (i == 10)
            c = '\n';
        if (c > 0 && c < '\200')
            keyArray[c] = 0;
    }

    public boolean dialogueSkipKeys(int key) {
        if (Client.instance.backDialogueId != -1 && !Client.instance.continuedDialogue
                && Client.instance.inputDialogState == 0 && !Client.instance.messagePromptRaised
                && !Console.consoleOpen) {
            int select = -1;
            if (key >= 49 && key <= 53) {
                int option = key - 49;
                switch (Client.instance.backDialogueId) {
                    case 2459:
                        select = 2461 + option;
                        break;
                    case 2469:
                        select = 2471 + option;
                        break;
                    case 2480:
                        select = 2482 + option;
                        break;
                    case 2492:
                        select = 2494 + option;
                        break;
                }
            }
            if (select != -1) {
                Client.instance.packetSender.sendButtonClick(select);
                return true;
            }
        }
        return false;
    }
}
