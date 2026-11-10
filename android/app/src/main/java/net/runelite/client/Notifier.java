package net.runelite.client;

import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.NotificationFired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows notifications to the user.
 *
 * <p>On the desktop this is a system-tray balloon (and an optional terminal
 * notifier). Android has no tray, so this variant keeps the notification
 * history — the in-game menu lists it — and hands the message to a
 * {@link Sink}, which the Android host wires up to a system notification.
 */
public class Notifier {

    /** Where a notification is shown. */
    public interface Sink {
        void show(String message, NotificationFired.Type type);
    }

    /** One notification in the history. */
    public static final class Notification {

        private final String message;

        private final NotificationFired.Type type;

        private final long time;

        Notification(String message, NotificationFired.Type type, long time) {
            this.message = message;
            this.type = type;
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public NotificationFired.Type getType() {
            return type;
        }

        public long getTime() {
            return time;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private static final int MAX_HISTORY = 50;

    private final EventBus eventBus;

    private final List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());

    private volatile Sink sink;

    private volatile boolean enabled = true;

    public Notifier(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    /** Whether notifications are shown at all. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void notify(String message) {
        notify(message, NotificationFired.Type.INFO);
    }

    public void notify(String message, NotificationFired.Type type) {
        if (message == null || message.isEmpty() || !enabled) {
            return;
        }
        notifications.add(0, new Notification(message, type, System.currentTimeMillis()));
        synchronized (notifications) {
            while (notifications.size() > MAX_HISTORY) {
                notifications.remove(notifications.size() - 1);
            }
        }
        if (eventBus != null) {
            eventBus.post(new NotificationFired(message, type));
        }
        Sink target = sink;
        if (target != null) {
            target.show(message, type);
        }
    }

    /** The notifications, newest first. */
    public List<Notification> getNotifications() {
        synchronized (notifications) {
            return new ArrayList<>(notifications);
        }
    }

    public void clear() {
        notifications.clear();
    }
}
