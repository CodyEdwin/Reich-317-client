package net.runelite.client.plugins.entityhider;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Android variant of RuneLite's Entity Hider configuration.
 *
 * <p>The desktop interface's default methods become concrete accessors backed
 * by {@link ConfigBase}; keys, names, defaults and order are unchanged so an
 * existing {@code runelite-config.json} keeps working.
 */
@ConfigGroup(EntityHiderConfig.GROUP)
public class EntityHiderConfig extends ConfigBase {

    public static final String GROUP = "entityhider";

    @ConfigItem(
            position = 1,
            keyName = "hidePlayers",
            name = "Hide Others",
            description = "Configures whether or not other players are hidden"
    )
    public boolean hideOthers() {
        return getBoolean("hidePlayers", true);
    }

    public void setHideOthers(boolean value) {
        set("hidePlayers", value);
    }

    @ConfigItem(
            position = 2,
            keyName = "hidePlayers2D",
            name = "Hide Others 2D",
            description = "Configures whether or not other players 2D elements are hidden"
    )
    public boolean hideOthers2D() {
        return getBoolean("hidePlayers2D", true);
    }

    public void setHideOthers2D(boolean value) {
        set("hidePlayers2D", value);
    }

    @ConfigItem(
            position = 3,
            keyName = "hideFriends",
            name = "Hide Friends",
            description = "Configures whether or not friends are hidden"
    )
    public boolean hideFriends() {
        return getBoolean("hideFriends", false);
    }

    public void setHideFriends(boolean value) {
        set("hideFriends", value);
    }

    @ConfigItem(
            position = 4,
            keyName = "hideClanMates",
            name = "Hide Friends Chat members",
            description = "Configures whether or not friends chat members are hidden"
    )
    public boolean hideFriendsChatMembers() {
        return getBoolean("hideClanMates", false);
    }

    public void setHideFriendsChatMembers(boolean value) {
        set("hideClanMates", value);
    }

    @ConfigItem(
            position = 5,
            keyName = "hideClanChatMembers",
            name = "Hide Clan Chat members",
            description = "Configures whether or not clan chat members are hidden"
    )
    public boolean hideClanChatMembers() {
        return getBoolean("hideClanChatMembers", false);
    }

    public void setHideClanChatMembers(boolean value) {
        set("hideClanChatMembers", value);
    }

    @ConfigItem(
            position = 6,
            keyName = "hideIgnores",
            name = "Hide Ignores",
            description = "Configures whether or not ignored players are hidden"
    )
    public boolean hideIgnores() {
        return getBoolean("hideIgnores", false);
    }

    public void setHideIgnores(boolean value) {
        set("hideIgnores", value);
    }

    @ConfigItem(
            position = 7,
            keyName = "hideLocalPlayer",
            name = "Hide Local Player",
            description = "Configures whether or not the local player is hidden"
    )
    public boolean hideLocalPlayer() {
        return getBoolean("hideLocalPlayer", false);
    }

    public void setHideLocalPlayer(boolean value) {
        set("hideLocalPlayer", value);
    }

    @ConfigItem(
            position = 8,
            keyName = "hideLocalPlayer2D",
            name = "Hide Local Player 2D",
            description = "Configures whether or not the local player's 2D elements are hidden"
    )
    public boolean hideLocalPlayer2D() {
        return getBoolean("hideLocalPlayer2D", false);
    }

    public void setHideLocalPlayer2D(boolean value) {
        set("hideLocalPlayer2D", value);
    }

    @ConfigItem(
            position = 9,
            keyName = "hideNPCs",
            name = "Hide NPCs",
            description = "Configures whether or not NPCs are hidden"
    )
    public boolean hideNPCs() {
        return getBoolean("hideNPCs", false);
    }

    public void setHideNPCs(boolean value) {
        set("hideNPCs", value);
    }

    @ConfigItem(
            position = 10,
            keyName = "hideNPCs2D",
            name = "Hide NPCs 2D",
            description = "Configures whether or not NPCs 2D elements are hidden"
    )
    public boolean hideNPCs2D() {
        return getBoolean("hideNPCs2D", false);
    }

    public void setHideNPCs2D(boolean value) {
        set("hideNPCs2D", value);
    }

    @ConfigItem(
            position = 11,
            keyName = "hidePets",
            name = "Hide Pets",
            description = "Configures whether or not other player pets are hidden"
    )
    public boolean hidePets() {
        return getBoolean("hidePets", false);
    }

    public void setHidePets(boolean value) {
        set("hidePets", value);
    }

    @ConfigItem(
            position = 12,
            keyName = "hideAttackers",
            name = "Hide Attackers",
            description = "Configures whether or not NPCs/players attacking you are hidden"
    )
    public boolean hideAttackers() {
        return getBoolean("hideAttackers", false);
    }

    public void setHideAttackers(boolean value) {
        set("hideAttackers", value);
    }

    @ConfigItem(
            position = 13,
            keyName = "hideProjectiles",
            name = "Hide Projectiles",
            description = "Configures whether or not projectiles are hidden"
    )
    public boolean hideProjectiles() {
        return getBoolean("hideProjectiles", false);
    }

    public void setHideProjectiles(boolean value) {
        set("hideProjectiles", value);
    }
}
