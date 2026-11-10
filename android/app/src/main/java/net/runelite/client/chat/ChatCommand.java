package net.runelite.client.chat;

import net.runelite.api.events.ChatMessage;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * A chat command a plugin registers.
 *
 * <p>Java port of the desktop class (which uses Lombok); the command is
 * triggered by typing {@code //name} in the chat.
 */
public class ChatCommand {

    private final String name;

    private final boolean async;

    private final BiConsumer<ChatMessage, String> execute;

    private final BiPredicate<ChatInput, String> input;

    public ChatCommand(String name, BiConsumer<ChatMessage, String> execute) {
        this(name, false, execute, null);
    }

    public ChatCommand(String name, boolean async, BiConsumer<ChatMessage, String> execute,
                       BiPredicate<ChatInput, String> input) {
        this.name = name;
        this.async = async;
        this.execute = execute;
        this.input = input;
    }

    public String getName() {
        return name;
    }

    public boolean isAsync() {
        return async;
    }

    public BiConsumer<ChatMessage, String> getExecute() {
        return execute;
    }

    public BiPredicate<ChatInput, String> getInput() {
        return input;
    }
}
