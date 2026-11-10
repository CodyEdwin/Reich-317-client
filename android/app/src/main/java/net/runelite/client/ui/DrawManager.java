package net.runelite.client.ui;

import android.graphics.Bitmap;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Android variant of RuneLite's {@code DrawManager}: runs listeners after every
 * frame and hands a frame snapshot to one-shot listeners.
 *
 * <p>The desktop version passes a {@code java.awt.Image}; Android uses a
 * {@link Bitmap} (the framebuffer snapshot).
 */
public class DrawManager {

    private final List<Runnable> everyFrame = new CopyOnWriteArrayList<>();
    private final Queue<Consumer<Bitmap>> nextFrame = new ConcurrentLinkedQueue<>();

    public void registerEveryFrameListener(Runnable everyFrameListener) {
        if (!everyFrame.contains(everyFrameListener)) {
            everyFrame.add(everyFrameListener);
        }
    }

    public void unregisterEveryFrameListener(Runnable everyFrameListener) {
        everyFrame.remove(everyFrameListener);
    }

    public void requestNextFrameListener(Consumer<Bitmap> nextFrameListener) {
        nextFrame.add(nextFrameListener);
    }

    /** Called once per rendered frame by the draw hook. */
    public void processDrawComplete(Supplier<Bitmap> imageSupplier) {
        for (Runnable everyFrameListener : everyFrame) {
            try {
                everyFrameListener.run();
            } catch (Exception e) {
                System.err.println("DrawManager: error in draw listener");
                e.printStackTrace();
            }
        }

        Consumer<Bitmap> nextFrameListener = nextFrame.poll();
        if (nextFrameListener == null) {
            return;
        }

        Bitmap image = null;
        if (imageSupplier != null) {
            try {
                image = imageSupplier.get();
            } catch (Exception e) {
                System.err.println("DrawManager: error getting screenshot");
                e.printStackTrace();
            }
        }

        if (image == null) {
            nextFrame.clear();
            return;
        }

        while (nextFrameListener != null) {
            try {
                nextFrameListener.accept(image);
            } catch (Exception e) {
                System.err.println("DrawManager: error in draw consumer");
                e.printStackTrace();
            }
            nextFrameListener = nextFrame.poll();
        }
    }
}
