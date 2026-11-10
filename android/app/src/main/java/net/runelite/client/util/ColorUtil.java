package net.runelite.client.util;

/**
 * Helpers for the Jagex colour tags the client understands.
 *
 * <p>Java port of the desktop {@code ColorUtil}: the colours are ARGB ints
 * instead of {@code java.awt.Color}, because Android has no AWT.
 */
public final class ColorUtil {

    public static final int MAX_RGB_VALUE = 255;
    public static final int MIN_RGB_VALUE = 0;

    private static final String OPENING_COLOR_TAG_START = "<col=";
    private static final String OPENING_COLOR_TAG_END = ">";
    public static final String CLOSING_COLOR_TAG = "</col>";

    private ColorUtil() {
    }

    /** Creates a colour tag from the given ARGB colour. */
    public static String colorTag(int argb) {
        return OPENING_COLOR_TAG_START + toHexColor(argb) + OPENING_COLOR_TAG_END;
    }

    /** Prepends the given string with an opening colour tag. */
    public static String prependColorTag(final String str, final int argb) {
        return colorTag(argb) + str;
    }

    /** Wraps the given string with a colour tag. */
    public static String wrapWithColorTag(final String str, final int argb) {
        return prependColorTag(str, argb) + CLOSING_COLOR_TAG;
    }

    /** The colour as a {@code #rrggbb} string. */
    public static String toHexColor(final int argb) {
        return String.format("#%06x", argb & 0xFFFFFF);
    }

    /** The colour as a {@code #aarrggbb} string. */
    public static String toHexColorWithAlpha(final int argb) {
        return String.format("#%08x", argb & 0xFFFFFFFFL);
    }

    /**
     * The colour of a {@code <col=...>} tag, or {@code null} when the string
     * does not start with one.
     */
    public static Integer colorFromTag(final String str) {
        if (str == null || !str.startsWith(OPENING_COLOR_TAG_START)) {
            return null;
        }
        int end = str.indexOf(OPENING_COLOR_TAG_END, OPENING_COLOR_TAG_START.length());
        if (end < 0) {
            return null;
        }
        String hex = str.substring(OPENING_COLOR_TAG_START.length(), end);
        try {
            long value = Long.parseLong(hex, 16);
            if (hex.length() <= 6) {
                value |= 0xFF000000L;
            }
            return (int) value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Strips a leading colour tag from the string. */
    public static String stripColorTag(final String str) {
        Integer color = colorFromTag(str);
        if (color == null) {
            return str;
        }
        int end = str.indexOf(OPENING_COLOR_TAG_END, OPENING_COLOR_TAG_START.length());
        return str.substring(end + 1);
    }
}
