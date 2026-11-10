package net.runelite.client.config;

/**
 * How a notification should be presented.
 */
public enum FlashNotification {

    DISABLED("Off"),
    FLASH_TWO_SECONDS("Flash for 2 seconds"),
    SOLID_TWO_SECONDS("Solid for 2 seconds"),
    FLASH_UNTIL_CANCELLED("Flash until cancelled"),
    SOLID_UNTIL_CANCELLED("Solid until cancelled");

    private final String type;

    FlashNotification(String type) {
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
