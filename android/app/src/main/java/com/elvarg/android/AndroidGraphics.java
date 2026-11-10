package com.elvarg.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.elvarg.android.gfx.Graphics2D;

/**
 * Creates {@link Graphics2D} instances that paint over the master software
 * framebuffer in {@link AndroidDisplay}.
 *
 * <p>RuneLite overlays draw through a {@code Graphics2D} whose output lands on
 * top of the freshly rendered game frame. On desktop that is the applet's AWT
 * {@code Graphics}; on Android we wrap the framebuffer in a scratch
 * {@link Bitmap}, let the overlay draw with a real {@link Canvas}, and copy the
 * result back into {@link AndroidDisplay#pixels} on {@link Graphics2D#dispose()}.
 *
 * <p>The client's pixels carry no alpha ({@code 0x00RRGGBB}), so the scratch
 * copy is made opaque before drawing: {@link Canvas} blends source alpha against
 * the destination, exactly as the desktop's alpha-less
 * {@code DirectColorModel} image did. The alpha byte is stripped again on
 * write-back by {@link Graphics2D#dispose()}.
 */
public final class AndroidGraphics {

    private static Bitmap scratchBitmap;
    private static int[] scratchPixels;

    private AndroidGraphics() {
    }

    public static Graphics2D begin() {
        int width = AndroidDisplay.width;
        int height = AndroidDisplay.height;

        // Reuse the scratch bitmap/pixels/Canvas: allocating a full-screen ARGB
        // bitmap every frame (1.5 MB at 765x503) causes constant GC pressure on
        // a device. The buffers are only touched from the game thread.
        if (scratchBitmap == null || scratchBitmap.getWidth() != width || scratchBitmap.getHeight() != height) {
            scratchBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            scratchPixels = new int[width * height];
        }
        // Overlays mutate paint state (colour, alpha, style, text size...), so
        // use a fresh paint per frame; it is tiny compared to the bitmap.
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Overlays draw text without setting a size (the desktop default font is
        // 12px); without this the paint's text size is 0 and text is invisible.
        paint.setTextSize(13f);

        int[] pixels = AndroidDisplay.pixels;
        int[] snapshot = scratchPixels;
        int count = Math.min(snapshot.length, pixels.length);
        System.arraycopy(pixels, 0, snapshot, 0, count);
        for (int i = 0; i < count; i++) {
            snapshot[i] |= 0xFF000000;
        }
        scratchBitmap.setPixels(snapshot, 0, width, 0, 0, width, height);

        // New Canvas each frame so overlays cannot leak clip/transform state
        // into the next frame; only the bitmap (the expensive part) is reused.
        return new Graphics2D(new Canvas(scratchBitmap), paint, scratchBitmap, pixels);
    }

    /**
     * Creates a graphics context that draws straight onto the given bitmap.
     *
     * <p>Used for the frame that is presented to the screen: the overlays are
     * drawn onto the buffer that holds the game frame, so the frame does not
     * have to be copied back afterwards.
     */
    public static Graphics2D begin(Bitmap target) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(13f);
        return new Graphics2D(new Canvas(target), paint, target, null);
    }
}
