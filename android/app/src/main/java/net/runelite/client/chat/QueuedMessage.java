package net.runelite.client.chat;

import net.runelite.api.ChatMessageType;

/**
 * A message that is waiting to be shown in the chat box.
 *
 * <p>Java port of the desktop class (which uses Lombok's {@code @Builder});
 * the builder is a plain static factory here.
 */
public class QueuedMessage {

    private final ChatMessageType type;

    private final String value;

    private final String name;

    private final String sender;

    private final String runeLiteFormattedMessage;

    private final int timestamp;

    private QueuedMessage(ChatMessageType type, String value, String name, String sender,
                          String runeLiteFormattedMessage, int timestamp) {
        this.type = type;
        this.value = value;
        this.name = name;
        this.sender = sender;
        this.runeLiteFormattedMessage = runeLiteFormattedMessage;
        this.timestamp = timestamp;
    }

    public static Builder builder(ChatMessageType type) {
        return new Builder(type);
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getSender() {
        return sender;
    }

    public String getRuneLiteFormattedMessage() {
        return runeLiteFormattedMessage;
    }

    public int getTimestamp() {
        return timestamp;
    }

    /** Builder for {@link QueuedMessage}. */
    public static final class Builder {

        private final ChatMessageType type;

        private String value;

        private String name;

        private String sender;

        private String runeLiteFormattedMessage;

        private int timestamp;

        private Builder(ChatMessageType type) {
            this.type = type;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder runeLiteFormattedMessage(String message) {
            this.runeLiteFormattedMessage = message;
            return this;
        }

        public Builder timestamp(int timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public QueuedMessage build() {
            return new QueuedMessage(type, value, name, sender, runeLiteFormattedMessage, timestamp);
        }
    }
}
