package net.runelite.client.config;

/**
 * Where a tooltip is drawn relative to the cursor.
 */
public enum TooltipPositionType {

    ABOVE_CURSOR("Above cursor"),
    UNDER_CURSOR("Under cursor");

    private final String type;

    TooltipPositionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
