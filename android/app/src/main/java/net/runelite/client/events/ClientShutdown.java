package net.runelite.client.events;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * Fired when the client is shutting down.
 *
 * <p>Consumers can register tasks that must finish before the client exits.
 */
public class ClientShutdown {

    private final Queue<Future<?>> tasks = new ConcurrentLinkedQueue<>();

    public void waitFor(Future<?> future) {
        tasks.add(future);
    }

    public void waitForAllConsumers(Duration totalTimeout) {
        long deadline = System.nanoTime() + totalTimeout.toNanos();
        for (Future<?> task; (task = tasks.poll()) != null; ) {
            long timeout = deadline - System.nanoTime();
            if (timeout < 0) {
                System.out.println("ClientShutdown: timed out waiting for task completion");
                return;
            }
            try {
                task.get(timeout, java.util.concurrent.TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                System.out.println("ClientShutdown: task failed");
                e.printStackTrace();
            }
        }
    }
}
