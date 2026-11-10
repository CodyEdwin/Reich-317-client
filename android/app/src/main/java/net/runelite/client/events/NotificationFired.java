package net.runelite.client.events;

/**
 * Fired when a notification should be shown to the user.
 *
 * <p>The desktop version carries a {@code java.awt.TrayIcon.MessageType};
 * Android has no tray, so the type is a small enum of its own.
 */
public class NotificationFired {

    public enum Type {
        INFO,
        WARNING,
        ERROR
    }

    private final String message;

    private final Type type;

    public NotificationFired(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }
}
