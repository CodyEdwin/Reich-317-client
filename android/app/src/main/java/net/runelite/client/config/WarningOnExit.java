package net.runelite.client.config;

/**
 * When the client should warn before it exits.
 */
public enum WarningOnExit {

    ALWAYS("Always"),
    LOGGED_IN("Logged in"),
    NEVER("Never");

    private final String type;

    WarningOnExit(String type) {
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
