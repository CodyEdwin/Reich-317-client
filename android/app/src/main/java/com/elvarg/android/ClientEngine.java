package com.elvarg.android;

import com.runescape.Client;
import com.runescape.engine.impl.KeyHandler;
import com.runescape.engine.impl.MouseHandler;
import com.runescape.engine.impl.MouseWheelHandler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Boots the real client ({@link Client}) on Android.
 *
 * <p>On the desktop the client is instantiated reflectively by the RuneLite
 * host ({@code net.runelite.client.rs.ClientLoader}) inside an applet container.
 * Here the same {@code com.runescape.Client} is created directly and driven by
 * the Android replacements for {@code GameEngine}/{@code Canvas}/{@code
 * RSImageProducer}. The cache directory must be set on
 * {@code com.runescape.sign.SignLink} before {@link #start()} (the service
 * copies the assets into internal storage first).
 *
 * <p>No Android imports here, so a desktop harness can boot the very same engine
 * and dump {@link AndroidDisplay} to a PNG for verification.
 */
public final class ClientEngine implements Runnable {

    /** The master framebuffer the app uploads to a Bitmap. */
    public final int[] pixels = AndroidDisplay.pixels;

    /**
     * Work posted from the UI thread (menu actions) that must run on the game
     * thread, drained from {@link AndroidCallbacks} on each tick/frame.
     */
    private static final Queue<Runnable> GAME_THREAD_TASKS = new ConcurrentLinkedQueue<>();

    public volatile boolean running = true;
    public volatile String status = "Starting";
    public volatile boolean clientStarted = false;

    private volatile Client game;
    private Thread thread;

    public void start() {
        if (thread != null) {
            return;
        }
        thread = new Thread(this, "elvarg-client");
        thread.setDaemon(true);
        thread.start();
    }

    public void shutdown() {
        running = false;
        Client c = game;
        if (c != null) {
            c.destroy();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            setStatus("Starting Elvarg client...");
            Client c = new Client();
            game = c;
            // init() opens the cache via SignLink, selects high memory, builds the
            // login screen and starts the original GameEngine game-loop thread.
            c.init();
            clientStarted = true;
            setStatus("Running");
        } catch (Throwable t) {
            setStatus("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void setStatus(String s) {
        status = s;
        System.out.println("[ElvargClient] " + s);
    }

    /**
     * Queues an action to run on the game thread. Plugin/config state is owned
     * by the game thread, so UI actions (enabling a plugin, editing a config,
     * logging out) are dispatched there rather than mutating it from the
     * activity thread. If the loop has not started yet the task runs on the
     * next tick/frame.
     */
    public static void post(Runnable task) {
        if (task != null) {
            GAME_THREAD_TASKS.add(task);
        }
    }

    /** Drains queued UI actions; called on the game thread only. */
    static void drainTasks() {
        Runnable task;
        while ((task = GAME_THREAD_TASKS.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("[ElvargClient] game-thread task failed");
                t.printStackTrace();
            }
        }
    }

    // ---- Input (canvas space) --------------------------------------------

    public void mouseDown(int x, int y, boolean metaDown) {
        MouseHandler.press(x, y, metaDown);
    }

    public void mouseUp() {
        MouseHandler.release();
    }

    public void mouseMove(int x, int y) {
        MouseHandler.move(x, y);
    }

    /** Pointer moved while held down (slider drags, camera drag). */
    public void mouseDrag(int x, int y) {
        MouseHandler.drag(x, y);
    }

    public void mouseWheel(int rotation) {
        MouseWheelHandler.push(rotation);
        // The desktop RuneLite host delivered wheel events through its AWT
        // MouseManager; on Android plugins get the neutral event instead.
        com.elvarg.android.rl.AndroidPluginHost.post(new net.runelite.api.events.MouseWheelMoved(rotation));
    }

    /** Tells the client whether the app is in the foreground. */
    public void focusChanged(boolean focused) {
        Client client = Client.instance;
        if (client != null) {
            client.notifyFocusChanged(focused);
        }
    }

    /** keyCode uses the AWT-style codes the client expects; see {@link KeyMap}. */    public void keyDown(int keyCode, char keyChar) {
        KeyHandler.press(keyCode, keyChar);
    }

    public void keyUp(int keyCode, char keyChar) {
        KeyHandler.release(keyCode, keyChar);
    }

    public Client game() {
        return game;
    }
}
