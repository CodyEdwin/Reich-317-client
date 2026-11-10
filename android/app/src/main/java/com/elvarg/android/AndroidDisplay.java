package com.elvarg.android;

import android.graphics.Bitmap;

import java.util.Arrays;

/**
 * The master software framebuffer for the Android client.
 *
 * <p>The desktop client draws into a {@code ProducingGraphicsBuffer} (an AWT
 * {@code BufferedImage} over an {@code int[]}) and blits it onto the applet with
 * {@code Graphics.drawImage}. Android has no AWT, so the Android
 * {@code ProducingGraphicsBuffer} instead composites its pixels into this
 * process-wide buffer, and {@link GameView} uploads it to a
 * {@link android.graphics.Bitmap}.
 *
 * <p>No Android imports here, so the same buffer can be driven from a desktop
 * harness for verification (mirroring the 317-world port).
 */
public final class AndroidDisplay {

    /** Fallback canvas size; the real size is set by {@link #setSize(int, int)}. */
    public static final int DEFAULT_WIDTH = 765;
    public static final int DEFAULT_HEIGHT = 503;

    /** 0x00RRGGBB pixels, exactly the layout the client's renderer writes. */
    public static int[] pixels = new int[DEFAULT_WIDTH * DEFAULT_HEIGHT];

    public static int width = DEFAULT_WIDTH;
    public static int height = DEFAULT_HEIGHT;

    /** Guards {@link #pixels} against the game thread and the UI thread. */
    public static final Object LOCK = new Object();

    /**
     * The composed frame (game + overlays) that is on screen, and the one that
     * is being built. The game thread fills the back buffer and swaps it in;
     * the UI thread only draws the front buffer, so a frame never has to be
     * copied again.
     */
    private static Bitmap frontBuffer;
    private static Bitmap backBuffer;
    private static int[] uploadPixels;

    private AndroidDisplay() {
    }

    /**
     * Copies the master framebuffer into the back buffer and returns it. The
     * overlays are drawn onto that buffer and {@link #presentFrame()} then
     * makes it the visible frame.
     *
     * <p>Call this from the game thread while holding {@link #LOCK}.
     */
    public static Bitmap beginFrame() {
        if (frontBuffer == null || frontBuffer.getWidth() != width || frontBuffer.getHeight() != height) {
            frontBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            uploadPixels = new int[width * height];
        }

        // The client's pixels carry no alpha, but the overlay drawing blends
        // against an opaque background, so the alpha byte is set here.
        int[] snapshot = uploadPixels;
        int count = Math.min(snapshot.length, pixels.length);
        System.arraycopy(pixels, 0, snapshot, 0, count);
        for (int i = 0; i < count; i++) {
            snapshot[i] |= 0xFF000000;
        }
        backBuffer.setPixels(snapshot, 0, width, 0, 0, width, height);
        return backBuffer;
    }

    /** Makes the frame built with {@link #beginFrame()} the visible one. */
    public static void presentFrame() {
        Bitmap temp = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = temp;
    }

    /** The frame that is on screen, or null before the first frame was drawn. */
    public static Bitmap frontBuffer() {
        return frontBuffer;
    }

    /**
     * Resizes the master buffer to the client's current canvas size. Called from
     * the Android game engine whenever the client resizes its raster provider.
     */
    public static void setSize(int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        synchronized (LOCK) {
            if (width == w && height == h) {
                return;
            }
            width = w;
            height = h;
            pixels = new int[w * h];
        }
    }

    public static void clear(int color) {
        synchronized (LOCK) {
            Arrays.fill(pixels, color);
        }
    }

    /** Fills an axis-aligned rectangle, clipped to the buffer (loading bars). */
    public static void fillRect(int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        synchronized (LOCK) {
            for (int row = 0; row < h; row++) {
                int dy = y + row;
                if (dy < 0 || dy >= height) {
                    continue;
                }
                int dx = x;
                int len = w;
                if (dx < 0) {
                    len += dx;
                    dx = 0;
                }
                if (dx + len > width) {
                    len = width - dx;
                }
                if (len <= 0) {
                    continue;
                }
                Arrays.fill(pixels, dy * width + dx, dy * width + dx + len, color);
            }
        }
    }

    /**
     * Opaque copy of a sub-rectangle of a producer's buffer at (x, y). Used by
     * the raster provider's partial {@code draw} path.
     */
    public static void blitRegion(int[] src, int srcWidth, int sx, int sy, int sw, int sh, int x, int y) {
        if (src == null || sw <= 0 || sh <= 0) {
            return;
        }
        synchronized (LOCK) {
            for (int row = 0; row < sh; row++) {
                int dy = y + row;
                if (dy < 0 || dy >= height) {
                    continue;
                }
                int dx = x;
                int len = sw;
                int srcOffset = (sy + row) * srcWidth + sx;
                if (dx < 0) {
                    srcOffset += -dx;
                    len += dx;
                    dx = 0;
                }
                if (dx + len > width) {
                    len = width - dx;
                }
                if (len <= 0) {
                    continue;
                }
                System.arraycopy(src, srcOffset, pixels, dy * width + dx, len);
            }
        }
    }

    /**
     * Opaque copy of a producer's buffer at (x, y), matching the desktop
     * {@code drawImage} of the client's no-alpha DirectColorModel. Off-screen
     * regions are clipped just as the window would clip them.
     */
    public static void blit(int[] src, int w, int h, int x, int y) {
        if (src == null) {
            return;
        }
        synchronized (LOCK) {
            for (int row = 0; row < h; row++) {
                int dy = y + row;
                if (dy < 0 || dy >= height) {
                    continue;
                }
                int srcOffset = row * w;
                int dx = x;
                int len = w;
                if (dx < 0) {
                    srcOffset += -dx;
                    len += dx;
                    dx = 0;
                }
                if (dx + len > width) {
                    len = width - dx;
                }
                if (len <= 0) {
                    continue;
                }
                System.arraycopy(src, srcOffset, pixels, dy * width + dx, len);
            }
        }
    }
}
