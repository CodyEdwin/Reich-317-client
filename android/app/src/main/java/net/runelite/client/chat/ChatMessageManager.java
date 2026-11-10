package net.runelite.client.chat;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Shows messages in the game's chat box.
 *
 * <p>The desktop version keeps the RuneLite chat colour configuration and
 * rewrites the message nodes of the OSRS client; the Elvarg client has its
 * own chat box and no message nodes, so this version simply forwards the
 * message into it. The colour tags the builder produces are understood by the
 * client's font renderer.
 */
public class ChatMessageManager {

    private final Client client;

    private final Queue<QueuedMessage> queuedMessages = new ConcurrentLinkedQueue<>();

    public ChatMessageManager(Client client) {
        this.client = client;
    }

    /** Queues a message; it is shown on the next {@link #process()}. */
    public void queue(QueuedMessage message) {
        if (message != null) {
            queuedMessages.add(message);
        }
    }

    /** Shows every queued message. Called from the game thread. */
    public void process() {
        for (QueuedMessage msg; (msg = queuedMessages.poll()) != null; ) {
            add(msg);
        }
    }

    /** Shows a message right away. */
    public void addMessage(ChatMessageType type, String message) {
        add(QueuedMessage.builder(type).value(message).build());
    }

    private void add(QueuedMessage message) {
        String text = message.getRuneLiteFormattedMessage() != null
                ? message.getRuneLiteFormattedMessage()
                : message.getValue();
        if (text == null || text.isEmpty()) {
            return;
        }
        client.addChatMessage(message.getType(),
                message.getName() == null ? "" : message.getName(),
                text,
                message.getSender());
    }
}
