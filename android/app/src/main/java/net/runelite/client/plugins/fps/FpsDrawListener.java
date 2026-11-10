package net.runelite.client.plugins.fps;

import net.runelite.api.events.FocusChanged;

/**
 * Enforces the configured frame rate.
 *
 * <p>Java port of the desktop {@code FpsDrawListener}: it runs after every
 * frame and sleeps for the remainder of the frame time. The same sampling
 * approach is used, because the client sometimes repaints after a single game
 * cycle and sometimes after many, which makes a naive per-frame delay
 * oscillate.
 */
public class FpsDrawListener implements Runnable {

    private static final int SAMPLE_SIZE = 4;

    private final FpsConfig config;

    private long targetDelay;

    /** Whether the app is in the foreground. */
    private boolean isFocused = true;

    private long lastMillis;

    private final long[] lastDelays = new long[SAMPLE_SIZE];

    private int lastDelayIndex;

    private long sleepDelay;

    public FpsDrawListener(FpsConfig config) {
        this.config = config;
        reloadConfig();
    }

    public void reloadConfig() {
        lastMillis = System.currentTimeMillis();

        int fps = config.limitFpsUnfocused() && !isFocused
                ? config.maxFpsUnfocused()
                : config.maxFps();

        targetDelay = 1000 / Math.max(1, fps);

        sleepDelay = targetDelay;

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            lastDelays[i] = targetDelay;
        }
    }

    public void onFocusChanged(FocusChanged event) {
        this.isFocused = event.isFocused();
        reloadConfig();
    }

    private boolean isEnforced() {
        return config.limitFps()
                || (config.limitFpsUnfocused() && !isFocused);
    }

    @Override
    public void run() {
        if (!isEnforced()) {
            return;
        }

        // The client's FPS counter is not frame-accurate, so the delay is
        // measured with the wall clock instead.
        final long before = lastMillis;
        final long now = System.currentTimeMillis();

        lastMillis = now;
        lastDelayIndex = (lastDelayIndex + 1) % SAMPLE_SIZE;
        lastDelays[lastDelayIndex] = now - before;

        long averageDelay = 0;
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            averageDelay += lastDelays[i];
        }
        averageDelay /= lastDelays.length;

        // The sleep delay is a moving target, because the engine decides when
        // to run its cycles, and plugins add time of their own.
        if (averageDelay > targetDelay) {
            sleepDelay--;
        } else if (averageDelay < targetDelay) {
            sleepDelay++;
        }

        if (sleepDelay > 0) {
            try {
                Thread.sleep(sleepDelay);
            } catch (InterruptedException e) {
                // Can happen on shutdown.
                Thread.currentThread().interrupt();
            }
        }
    }
}
