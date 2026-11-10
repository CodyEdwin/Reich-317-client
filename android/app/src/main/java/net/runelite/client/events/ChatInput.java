package net.runelite.client.chat;

/**
 * A chat input a plugin can take over.
 *
 * <p>Java port of the desktop class.
 */
public abstract class ChatInput {

    /** Gives the chat input back to the client. */
    public abstract void resume();
}
