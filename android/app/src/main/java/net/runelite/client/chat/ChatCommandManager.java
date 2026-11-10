package net.runelite.client.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The chat commands the plugins registered.
 *
 * <p>RuneLite's desktop version hooks into the OSRS chatbox script; this
 * client has its own chat, so the commands are dispatched by the client
 * itself: typing {@code //name} in the chat runs the command instead of
 * sending the message to the server.
 */
public class ChatCommandManager {

    private final Map<String, ChatCommand> commands = new ConcurrentHashMap<>();

    /** Registers a command; the name is the one the user types after {@code //}. */
    public void registerCommand(String name, ChatCommand command) {
        if (name == null || command == null) {
            return;
        }
        commands.put(name.toLowerCase(), command);
    }

    public void registerCommand(String name,
                                 java.util.function.BiConsumer<net.runelite.api.events.ChatMessage, String> execute) {
        registerCommand(name, new ChatCommand(name, execute));
    }

    public void unregisterCommand(String name) {
        if (name != null) {
            commands.remove(name.toLowerCase());
        }
    }

    public Map<String, ChatCommand> getCommands() {
        return commands;
    }

    /**
     * Runs the command in the given line (without the leading slashes).
     *
     * @return true when a command handled the line
     */
    public boolean runCommand(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.isEmpty()) {
            return false;
        }

        int space = trimmed.indexOf(' ');
        String name = (space < 0 ? trimmed : trimmed.substring(0, space)).toLowerCase();
        String args = space < 0 ? "" : trimmed.substring(space + 1);

        ChatCommand command = commands.get(name);
        if (command == null) {
            return false;
        }
        try {
            command.getExecute().accept(null, args);
        } catch (Exception e) {
            System.err.println("ChatCommandManager: command '" + name + "' failed");
            e.printStackTrace();
        }
        return true;
    }
}
