package com.runescape.loginscreen;

import com.elvarg.android.beans.ChangeListener;
import com.elvarg.android.beans.DoubleProperty;
import com.elvarg.android.beans.ObservableValue;
import com.elvarg.android.beans.SimpleDoubleProperty;
import com.runescape.Client;
import com.runescape.Configuration;
import com.runescape.UserPreferences;
import com.runescape.cache.graphics.ImageCache;
import com.runescape.draw.Rasterizer2D;
import com.runescape.engine.GameEngine;
import com.runescape.engine.impl.KeyHandler;
import com.runescape.loginscreen.worlds.WorldData;
import com.runescape.loginscreen.worlds.WorldManager;
import com.runescape.util.MiscUtils;
import com.runescape.util.StringUtils;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The login screen: EULA, welcome, username/password and the world list.
 *
 * <p>Java port of the desktop Kotlin class (the desktop sources are
 * untouched). The behaviour, layout and asset ids are identical; only the
 * language changed.
 */
public class LoginScreen {

    public static final int BACKGROUND_ACTIVE_TIME_SECONDS = 10;

    private static final String VALID_USER_PASS_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\u00a3$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

    private static final List<String> EULA_TEXT = Arrays.asList(
            "Before using this app, please read and accept our",
            "@gol@terms of use@whi@, @gol@privacy policy@whi@, and @gol@end user licence",
            "@gol@agreement (EULA)@whi@.",
            "By accepting, you agree to these documents.");

    private final Client client;

    private StopWatch backgroundStopWatch;

    private LoginState loginState = LoginState.LOGIN;

    private int opacity = 0;

    private int backgroundSprite = LoginBackground.NORMAL.getSpriteID();

    private final DoubleProperty fadeToBlack = new SimpleDoubleProperty(256.0);

    public LoginScreen(Client client) {
        this.client = client;
    }

    public void setUp() {
        fadeToBlack.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() >= 256) {
                    backgroundSprite = backgroundSprite();
                    if (backgroundStopWatch == null) {
                        backgroundStopWatch = new StopWatch();
                    }
                    backgroundStopWatch.reset();
                    backgroundStopWatch.start();
                }
                if (newValue.doubleValue() <= 0) {
                    fadeToBlack.set(256.0);
                }
            }
        });
    }

    public LoginState getLoginState() {
        return loginState;
    }

    public void setLoginState(LoginState loginState) {
        this.loginState = loginState;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void drawLogin() {
        WorldManager worldManager = WorldManager.INSTANCE;
        if (worldManager.getSelectedWorld() == null) {
            worldManager.openWorldSectionScreen(false);
            List<WorldData> worlds = worldManager.getWorldList();
            if (!worlds.isEmpty()) {
                worldManager.setSelectedWorld(worlds.get(0));
            }
        }

        final int centerX = GameEngine.canvasWidth / 2;
        final int centerY = GameEngine.canvasHeight / 2;
        final int alpha = Client.preferences.getLoginBackground() != LoginBackground.FADING_BACKGROUNDS
                ? 226 : opacity;
        handleBackgrounds();
        if (Client.preferences.getLoginBackground() != LoginBackground.ANIMATED_GAME_WORLD) {
            Rasterizer2D.drawBox(0, 0, GameEngine.canvasWidth, GameEngine.canvasHeight, 0x000000);
            if (backgroundSprite != -1) {
                ImageCache.get(backgroundSprite).drawAdvancedSprite(centerX - (766 / 2), centerY - (503 / 2), alpha);
            }
            Client.loginScreenRunesAnimation.draw(centerX - (766 / 2) - 15, 0, Client.tick, alpha);
            Client.loginScreenRunesAnimation.draw(GameEngine.canvasWidth - 110, 0, Client.tick, alpha);
        }
        ImageCache.get(0).drawAdvancedSprite(centerX - (444 / 2), centerY - (GameEngine.canvasHeight / 2) + 17);
        ImageCache.get(1).drawSprite(centerX - (360 / 2), centerY - (200 / 2) + 21);

        final int loginBoxX = centerX - (360 / 2);
        final int loginBoxY = centerY - (200 / 2) + 21;

        ImageCache.get(Client.preferences.getEnableMusic() ? 25 : 26)
                .drawAdvancedSprite(GameEngine.canvasWidth - 38 - 5, GameEngine.canvasHeight - 45 + 7);
        if (worldManager.isLoadedWorlds()) {
            ImageCache.get(3).drawAdvancedSprite(centerX - (GameEngine.canvasWidth / 2) + 5,
                    GameEngine.canvasHeight - 45 + 8);
            WorldData selected = worldManager.getSelectedWorld();
            client.newBoldFont.drawCenteredString("World: " + (selected == null ? "" : selected.getName()),
                    centerX - (GameEngine.canvasWidth / 2) + 5 + (100 / 2),
                    GameEngine.canvasHeight - 45 + 23, 0xFFFFFF, 1);
            client.newSmallFont.drawCenteredString(worldManager.getWorldStatusText(),
                    centerX - (GameEngine.canvasWidth / 2) + 5 + (100 / 2),
                    GameEngine.canvasHeight - 45 + 38, 0xFFFFFF, 1);
        }

        switch (loginState) {
            case EULA: {
                for (int index = 0; index < EULA_TEXT.size(); index++) {
                    client.newRegularFont.drawCenteredString(EULA_TEXT.get(index),
                            loginBoxX + (360 / 2), loginBoxY + 43 + (20 * index), 0xFFFFFF, 1);
                }
                String[] buttons = {"Accept", "Decline"};
                for (int index = 0; index < buttons.length; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    ImageCache.get(2).drawSprite(buttonX, loginBoxY + 121);
                    client.newBoldFont.drawCenteredString(buttons[index],
                            buttonX + (147 / 2) - 1, loginBoxY + 146, 0xFFFFFF, 1);
                }
                break;
            }
            case WELCOME: {
                client.newBoldFont.drawCenteredString("Welcome to " + Configuration.CLIENT_NAME,
                        loginBoxX + (360 / 2), loginBoxY + 82, 0xFFFF00, 1);

                String[] buttons = {"New User", "Existing User"};
                for (int index = 0; index < buttons.length; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    ImageCache.get(2).drawSprite(buttonX - 1, loginBoxY + 100);
                    client.newBoldFont.drawCenteredString(buttons[index],
                            buttonX + (147 / 2) - 3, loginBoxY + 124, 0xFFFFFF, 1);
                }
                break;
            }
            case LOGIN: {
                if (!client.firstLoginMessage.isEmpty()) {
                    client.newBoldFont.drawCenteredString(client.firstLoginMessage,
                            loginBoxX + (360 / 2), loginBoxY + 56 - 17, 0xFFFF00, 1);
                    client.newBoldFont.drawCenteredString(client.secondLoginMessage,
                            loginBoxX + (360 / 2), loginBoxY + 70 - 17, 0xFFFF00, 1);
                } else {
                    client.newBoldFont.drawCenteredString(client.secondLoginMessage,
                            loginBoxX + (360 / 2), loginBoxY + 63 - 17, 0xFFFF00, 1);
                }

                String[] buttons = {"Login", "Cancel"};
                for (int index = 0; index < buttons.length; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    ImageCache.get(2).drawSprite(buttonX, loginBoxY + 131);
                    client.newBoldFont.drawCenteredString(buttons[index],
                            buttonX + (147 / 2) - 1, loginBoxY + 156, 0xFFFFFF, 1);
                }

                client.newBoldFont.drawBasicString("Login: ", loginBoxX + 70, loginBoxY + 83, 0xFFFFFF, 1);
                client.newBoldFont.drawBasicString("Password: ", loginBoxX + 72, loginBoxY + 98, 0xFFFFFF, 1);

                client.newBoldFont.drawBasicString(
                        (Client.preferences.getHiddenUsername() ? client.myUsername
                                : StringUtils.passwordAsterisks(client.myUsername)) + flash(0),
                        loginBoxX + 110, loginBoxY + 83, 0xFFFFFF, 1);

                client.newBoldFont.drawBasicString(
                        StringUtils.passwordAsterisks(client.myPassword) + flash(1),
                        loginBoxX + 123 + 20, loginBoxY + 98, 0xFFFFFF, 1);

                ImageCache.get(Client.preferences.getRememberUsername() ? 23 : 21)
                        .drawHoverSprite(loginBoxX + 63, loginBoxY + 107,
                                ImageCache.get(Client.preferences.getRememberUsername() ? 24 : 22));
                ImageCache.get(Client.preferences.getHiddenUsername() ? 23 : 21)
                        .drawHoverSprite(loginBoxX + 204, loginBoxY + 107,
                                ImageCache.get(Client.preferences.getHiddenUsername() ? 24 : 22));

                client.newSmallFont.drawBasicString("Remember Username",
                        loginBoxX + 63 + 22, loginBoxY + 119, 0xFFFF00, 1);

                client.newSmallFont.drawBasicString("Hide Username",
                        loginBoxX + 204 + 22, loginBoxY + 119, 0xFFFF00, 1);
                break;
            }
            case WORLD_SELECT:
                worldManager.renderWorldSelect();
                break;
            default:
                break;
        }
    }

    public void handleInput() {
        final int centerX = GameEngine.canvasWidth / 2;
        final int centerY = GameEngine.canvasHeight / 2;
        final int loginBoxX = centerX - (360 / 2);
        final int loginBoxY = centerY - (200 / 2) + 21;

        if (client.newclickInRegion(centerX - (GameEngine.canvasWidth / 2) + 5,
                GameEngine.canvasHeight - 45 + 8, ImageCache.get(3))) {
            WorldManager.INSTANCE.openWorldSectionScreen(true);
        }

        if (client.newclickInRegion(GameEngine.canvasWidth - 38 - 5,
                GameEngine.canvasHeight - 45 + 7, ImageCache.get(25))) {
            Client.preferences.setEnableMusic(!Client.preferences.getEnableMusic());
        }

        switch (loginState) {
            case EULA: {
                for (int index = 0; index < 2; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    if (client.newclickInRegion(buttonX + (147 / 2) - 1, loginBoxY + 146, ImageCache.get(2))) {
                        if (index == 0) {
                            loginState = LoginState.LOGIN;
                            Client.preferences.setEulaAccepted(true);
                            UserPreferences.INSTANCE.save();
                        } else {
                            System.exit(0);
                        }
                    }
                }
                break;
            }
            case WORLD_SELECT:
                WorldManager.INSTANCE.handleInput();
                break;
            case WELCOME: {
                for (int index = 0; index < 2; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    if (client.newclickInRegion(buttonX - 1, loginBoxY + 100, ImageCache.get(2))) {
                        if (index == 0) {
                            MiscUtils.launchURL("https://www.google.com/");
                        } else {
                            loginState = LoginState.LOGIN;
                        }
                    }
                }
                break;
            }
            case LOGIN: {
                for (int index = 0; index < 2; index++) {
                    int buttonX = loginBoxX + 28 + (index == 1 ? 160 : 0);
                    if (client.newclickInRegion(buttonX, loginBoxY + 131, ImageCache.get(2))) {
                        if (index == 0) {
                            client.login(client.myUsername, client.myPassword, false);
                        } else {
                            loginState = LoginState.WELCOME;
                        }
                    }
                }

                if (client.newclickInRegion(loginBoxX + 110, loginBoxY + 70, 200, 15)) {
                    client.loginScreenCursorPos = 0;
                }

                if (client.newclickInRegion(loginBoxX + 140, loginBoxY + 87, 200, 15)) {
                    client.loginScreenCursorPos = 1;
                }

                if (client.newclickInRegion(loginBoxX + 63, loginBoxY + 107, ImageCache.get(21))) {
                    Client.preferences.setRememberUsername(!Client.preferences.getRememberUsername());
                }

                if (client.newclickInRegion(loginBoxX + 204, loginBoxY + 107, ImageCache.get(21))) {
                    Client.preferences.setHiddenUsername(!Client.preferences.getHiddenUsername());
                }

                while (true) {
                    int typed = KeyHandler.instance.readChar();
                    if (typed == -1) {
                        break;
                    }
                    boolean valid = false;
                    for (int i = 0; i < VALID_USER_PASS_CHARS.length(); i++) {
                        if (typed != VALID_USER_PASS_CHARS.charAt(i)) {
                            continue;
                        }
                        valid = true;
                        break;
                    }

                    if (client.loginScreenCursorPos == 0) {
                        if (typed == 8 && !client.myUsername.isEmpty()) {
                            client.myUsername = client.myUsername.substring(0, client.myUsername.length() - 1);
                        }
                        if (typed == 9 || typed == 10 || typed == 13) {
                            client.loginScreenCursorPos = 1;
                        }
                        if (valid) {
                            client.myUsername += (char) typed;
                        }
                        if (client.myUsername.length() > 12) {
                            client.myUsername = client.myUsername.substring(0, 12);
                        }
                    } else if (client.loginScreenCursorPos == 1) {
                        if (typed == 8 && !client.myPassword.isEmpty()) {
                            client.myPassword = client.myPassword.substring(0, client.myPassword.length() - 1);
                        }
                        if (typed == 9 || typed == 10 || typed == 13) {
                            if (!client.myPassword.isEmpty()) {
                                client.loginFailures = 0;
                                client.login(client.myUsername, client.myPassword, false);
                            } else {
                                client.loginScreenCursorPos = 0;
                            }
                        }
                        if (valid) {
                            client.myPassword += (char) typed;
                        }
                        if (client.myPassword.length() > 15) {
                            client.myPassword = client.myPassword.substring(0, 15);
                        }
                    }
                }
                return;
            }
            default:
                break;
        }
    }

    private int backgroundSprite() {
        LoginBackground background = Client.preferences.getLoginBackground();
        if (background == LoginBackground.FADING_BACKGROUNDS) {
            List<LoginBackground> withSprite = new ArrayList<>();
            for (LoginBackground candidate : LoginBackground.values()) {
                if (candidate.getSpriteID() != -1) {
                    withSprite.add(candidate);
                }
            }
            if (withSprite.isEmpty()) {
                return -1;
            }
            return withSprite.get((int) (Math.random() * withSprite.size())).getSpriteID();
        }
        return background.getSpriteID();
    }

    private void handleBackgrounds() {
        LoginBackground background = Client.preferences.getLoginBackground();
        if (background == LoginBackground.FADING_BACKGROUNDS) {
            if (backgroundStopWatch == null) {
                backgroundStopWatch = new StopWatch();
                backgroundStopWatch.start();
            }

            long end = backgroundStopWatch.getStartTime() + 1000L * BACKGROUND_ACTIVE_TIME_SECONDS;
            long increment = (end - backgroundStopWatch.getStartTime()) / 100;
            if (increment > 0) {
                fadeToBlack.set(fadeToBlack.get() - 0.9);
                opacity = (int) Math.ceil(fadeToBlack.get());
            }
        } else if (background == LoginBackground.ANIMATED_GAME_WORLD) {
            Client.instance.cinematicScene.render();
        } else {
            backgroundSprite = backgroundSprite();
        }
    }

    private String flash(int state) {
        return (client.loginScreenCursorPos == state && Client.tick % 40 < 20) ? "@yel@|" : "";
    }
}
