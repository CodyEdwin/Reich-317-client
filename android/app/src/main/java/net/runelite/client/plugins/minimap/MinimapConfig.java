package net.runelite.client.plugins.minimap;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

/**
 * Android variant of RuneLite's Minimap configuration.
 *
 * <p>The desktop config returns {@code java.awt.Color} values; Android has no
 * AWT, so the colours are plain ARGB ints (stored as {@code AARRGGBB} hex)
 * and the settings editor paints a swatch for them. A value of {@code 0}
 * means "keep the vanilla dot".
 */
@ConfigGroup(MinimapConfig.GROUP)
public class MinimapConfig extends ConfigBase {

    public static final String GROUP = "minimap";

    @ConfigSection(
            name = "Minimap dot colors",
            description = "The colors of dots on the minimap.",
            position = 0
    )
    public static final String MINIMAP_DOT_SECTION = "minimapDotSection";

    @ConfigItem(
            keyName = "zoom",
            name = "Zoom",
            description = "Enables zooming on the minimap"
    )
    public boolean zoom() {
        return getBoolean("zoom", true);
    }

    public void setZoom(boolean value) {
        set("zoom", value);
    }

    @ConfigItem(
            keyName = "hideMinimap",
            name = "Hide minimap",
            description = "Do not show the minimap on screen (Resizable only)"
    )
    public boolean hideMinimap() {
        return getBoolean("hideMinimap", false);
    }

    public void setHideMinimap(boolean value) {
        set("hideMinimap", value);
    }

    @ConfigItem(
            keyName = "hdminimap",
            name = "HD Minimap",
            description = "Shows a more detailed minimap"
    )
    public boolean hdMinimap() {
        return getBoolean("hdminimap", false);
    }

    public void setHdMinimap(boolean value) {
        set("hdminimap", value);
    }

    @ConfigItem(
            keyName = "item",
            name = "Item color",
            description = "Set the minimap color items are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int itemColor() {
        return getColor("item", 0);
    }

    @ConfigItem(
            keyName = "npc",
            name = "NPC color",
            description = "Set the minimap color NPCs are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int npcColor() {
        return getColor("npc", 0);
    }

    @ConfigItem(
            keyName = "player",
            name = "Player color",
            description = "Set the minimap Color players are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int playerColor() {
        return getColor("player", 0);
    }

    @ConfigItem(
            keyName = "friend",
            name = "Friends color",
            description = "Set the minimap color your friends are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int friendColor() {
        return getColor("friend", 0);
    }

    @ConfigItem(
            keyName = "team",
            name = "Team color",
            description = "Set the minimap color your team is drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int teamColor() {
        return getColor("team", 0);
    }

    @ConfigItem(
            keyName = "clan",
            name = "Friends Chat color",
            description = "Set the minimap color your friends chat members are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int friendsChatColor() {
        return getColor("clan", 0);
    }

    @ConfigItem(
            keyName = "clanchat",
            name = "Clan Chat color",
            description = "Set the minimap color your clan chat members are drawn in",
            section = MINIMAP_DOT_SECTION,
            color = true
    )
    public int clanChatColor() {
        return getColor("clanchat", 0);
    }
}
