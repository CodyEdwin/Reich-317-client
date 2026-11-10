package net.runelite.client.chat;

import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

/**
 * Builds a chat message.
 *
 * <p>Java port of the desktop class: the colours are ARGB ints instead of
 * {@code java.awt.Color}, because Android has no AWT.
 */
public class ChatMessageBuilder {

    private final StringBuilder builder = new StringBuilder();

    public ChatMessageBuilder append(final ChatColorType type) {
        builder.append("<col").append(type.name()).append(">");
        return this;
    }

    public ChatMessageBuilder append(final int argb, final String message) {
        builder.append(ColorUtil.wrapWithColorTag(message, argb));
        return this;
    }

    public ChatMessageBuilder append(final String message) {
        builder.append(Text.escapeJagex(message));
        return this;
    }

    public ChatMessageBuilder img(int imageId) {
        builder.append("<img=").append(imageId).append('>');
        return this;
    }

    public String build() {
        return builder.toString();
    }
}
