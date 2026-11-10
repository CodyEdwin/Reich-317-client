package net.runelite.client.callback;

import net.runelite.api.Client;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

/**
 * Runs code on the game thread.
 *
 * <p>Same contract as the desktop version: {@link #invoke(Runnable)} runs the
 * task as soon as possible (immediately when the caller already is the game
 * thread), {@link #invokeLater(Runnable)} always queues it, and a task that
 * returns {@code false} is run again later.
 *
 * <p>Java port: the desktop class uses Guice/Lombok, the Android host wires
 * it up explicitly.
 */
public class ClientThread {

    private final ConcurrentLinkedQueue<BooleanSupplier> invokes = new ConcurrentLinkedQueue<>();

    private final Client client;

    public ClientThread(Client client) {
        this.client = client;
    }

    public void invoke(Runnable r) {
        invoke(() -> {
            r.run();
            return true;
        });
    }

    /**
     * Will run r on the game thread, at an unspecified point in the future.
     * If r returns false, r will be run again, at a later point.
     */
    public void invoke(BooleanSupplier r) {
        if (client.isClientThread()) {
            if (!r.getAsBoolean()) {
                invokes.add(r);
            }
            return;
        }

        invokeLater(r);
    }

    /**
     * Will run r on the game thread after this method returns.
     */
    public void invokeLater(Runnable r) {
        invokeLater(() -> {
            r.run();
            return true;
        });
    }

    public void invokeLater(BooleanSupplier r) {
        invokes.add(r);
    }

    /** Runs the queued tasks; called from the game loop. */
    public void invoke() {
        Iterator<BooleanSupplier> ir = invokes.iterator();
        while (ir.hasNext()) {
            BooleanSupplier r = ir.next();
            boolean remove = true;
            try {
                remove = r.getAsBoolean();
            } catch (Throwable e) {
                System.err.println("ClientThread: exception in invoke");
                e.printStackTrace();
            }
            if (remove) {
                ir.remove();
            }
        }
    }
}
