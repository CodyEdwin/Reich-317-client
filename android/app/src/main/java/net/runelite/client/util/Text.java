package net.runelite.client.util;

/**
 * Text helpers shared by the plugins.
 *
 * <p>Java port of the desktop {@code Text} class (only the parts that do not
 * need AWT).
 */
public final class Text {

    private Text() {
    }

    /**
     * Escapes the characters the client would otherwise interpret, so a
     * message can be shown verbatim.
     */
    public static String escapeJagex(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '<') {
                out.append("<lt>");
            } else if (c == '>') {
                out.append("<gt>");
            } else if (c == '\n') {
                out.append("<br>");
            } else if (c != '\r') {
                out.append(c);
            }
        }

        return out.toString();
    }

    /** Removes the Jagex colour tags from a string. */
    public static String removeTags(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("<[^>]*>", "");
    }

    /** Splits a string into lines of at most {@code length} characters. */
    public static String[] wrap(String str, int length) {
        if (str == null) {
            return new String[0];
        }
        int count = (str.length() + length - 1) / Math.max(1, length);
        String[] lines = new String[count];
        for (int i = 0; i < count; i++) {
            int start = i * length;
            int end = Math.min(str.length(), start + length);
            lines[i] = str.substring(start, end);
        }
        return lines;
    }
}
