package net.runelite.client.config;

/**
 * The client-wide settings.
 *
 * <p>Java port of the desktop {@code RuneLiteConfig}. The desktop config is
 * mostly about the Swing window (size, opacity, custom chrome, screen
 * bounds); on Android the window is the phone screen, so only the settings
 * that still apply are kept. The group name is the same as on desktop
 * ({@code runelite}), so the enabled-plugin state and these settings share
 * one group.
 */
@ConfigGroup(RuneLiteConfig.GROUP_NAME)
public class RuneLiteConfig extends ConfigBase {

    public static final String GROUP_NAME = "runelite";

    @ConfigSection(
            name = "Client Settings",
            description = "Settings that apply to the whole client",
            position = 0
    )
    public static final String CLIENT_SETTINGS = "clientSettings";

    @ConfigSection(
            name = "Notification Settings",
            description = "Settings relating to notifications",
            position = 1
    )
    public static final String NOTIFICATION_SETTINGS = "notificationSettings";

    @ConfigItem(
            keyName = "showOnScreenBar",
            name = "Show on-screen buttons",
            description = "Shows the menu, keyboard and right-click buttons over the game",
            position = 1,
            section = CLIENT_SETTINGS
    )
    public boolean showOnScreenBar() {
        return getBoolean("showOnScreenBar", true);
    }

    public void setShowOnScreenBar(boolean value) {
        set("showOnScreenBar", value);
    }

    @ConfigItem(
            keyName = "enableNotifications",
            name = "Enable notifications",
            description = "Shows notifications raised by plugins",
            position = 1,
            section = NOTIFICATION_SETTINGS
    )
    public boolean enableNotifications() {
        return getBoolean("enableNotifications", true);
    }

    public void setEnableNotifications(boolean value) {
        set("enableNotifications", value);
    }
}
