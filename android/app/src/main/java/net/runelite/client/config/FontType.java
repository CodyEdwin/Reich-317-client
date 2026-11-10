package net.runelite.client.config;

/**
 * The font styles the client can draw text with.
 *
 * <p>The desktop enum carries an {@code java.awt.Font}; Android has no AWT, so
 * only the display name is kept and the overlay draws the text itself.
 */
public enum FontType {

    REGULAR("Regular"),
    BOLD("Bold"),
    SMALL("Small");

    private final String name;

    FontType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
