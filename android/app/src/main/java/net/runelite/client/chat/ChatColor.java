package net.runelite.client.chat;

/**
 * A chat colour.
 *
 * <p>Java port of the desktop class: the colour is an ARGB int instead of a
 * {@code java.awt.Color}, because Android has no AWT.
 */
public class ChatColor {

    private final ChatColorType type;

    private final int color;

    private final boolean transparent;

    private final boolean isDefault;

    public ChatColor(ChatColorType type, int color, boolean transparent) {
        this(type, color, transparent, false);
    }

    public ChatColor(ChatColorType type, int color, boolean transparent, boolean isDefault) {
        this.type = type;
        this.color = color;
        this.transparent = transparent;
        this.isDefault = isDefault;
    }

    public ChatColorType getType() {
        return type;
    }

    /** The colour as an ARGB int. */
    public int getColor() {
        return color;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatColor)) {
            return false;
        }
        ChatColor other = (ChatColor) o;
        return transparent == other.transparent
                && type == other.type
                && isDefault == other.isDefault;
    }

    @Override
    public int hashCode() {
        return (type == null ? 0 : type.hashCode()) * 31 + (transparent ? 1 : 0);
    }
}
