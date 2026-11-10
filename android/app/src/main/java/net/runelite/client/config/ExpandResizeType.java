package net.runelite.client.config;

/**
 * What a resizable-mode resize keeps constant.
 */
public enum ExpandResizeType {

    KEEP_WINDOW_SIZE("Keep window size"),
    KEEP_GAME_SIZE("Keep game size");

    private final String type;

    ExpandResizeType(String type) {
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
