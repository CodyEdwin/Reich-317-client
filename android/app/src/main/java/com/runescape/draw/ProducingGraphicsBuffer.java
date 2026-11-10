package com.runescape.draw;

import android.graphics.Bitmap;

import com.elvarg.android.AndroidDisplay;
import com.elvarg.android.AndroidGraphics;
import com.elvarg.android.rl.AndroidPluginHost;
import com.elvarg.android.gfx.Graphics2D;
import com.runescape.Client;
import com.runescape.engine.Canvas;
import net.runelite.rs.api.RSRasterProvider;

/**
 * Android replacement for the desktop {@code ProducingGraphicsBuffer}.
 *
 * <p>The desktop class wraps the client's {@code int[]} pixels in an AWT
 * {@code BufferedImage} sharing that array, so the client draws straight into a
 * window image. Android has no AWT, so this version keeps the identical pixel
 * buffer and drawing flow ({@link Rasterizer2D} draws into {@link #pixels}) but
 * blits the finished frame into {@link AndroidDisplay}, which {@code GameView}
 * uploads to a Bitmap. The RuneLite overlay callback still runs, over a
 * {@link Graphics2D} backed by the framebuffer.
 */
public class ProducingGraphicsBuffer extends AbstractRasterProvider implements RSRasterProvider {
    private Canvas component;
    private Bitmap image;

    public ProducingGraphicsBuffer(int width, int height, Canvas component) {
        super.width = width;
        super.height = height;
        super.pixels = new int[height * width + 1];
        this.setComponent(component);
        AndroidDisplay.setSize(width, height);
        this.apply();
    }

    public final void setComponent(Canvas var1) {
        this.component = var1;
    }

    public void drawFull(int var1, int var2) {
        // Present the frame atomically. The UI thread draws the front buffer
        // while holding LOCK; blitting, painting the overlays and swapping the
        // buffers must happen as one step, otherwise a frame without its
        // overlays can be displayed.
        synchronized (AndroidDisplay.LOCK) {
            AndroidDisplay.blit(pixels, width, height, var1, var2);

            // The frame is composed in a bitmap that the UI thread can draw
            // directly, so it never has to be copied again.
            android.graphics.Bitmap frame = AndroidDisplay.beginFrame();

            // RuneLite draw hook: overlays are painted over the freshly blitted
            // frame.
            if (AndroidPluginHost.hasOverlays()) {
                Graphics2D graphics = AndroidGraphics.begin(frame);
                try {
                    Client.instance.getCallbacks().draw(this, graphics, var1, var2);
                } finally {
                    graphics.dispose();
                }
            }

            AndroidDisplay.presentFrame();
        }
    }

    public final void draw(int var1, int var2, int var3, int var4) {
        AndroidDisplay.blitRegion(pixels, width, var1, var2, var3, var4, var1, var2);
    }

    @Override
    public int[] getPixels() {
        return pixels;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setRaster() {
        Rasterizer2D.initDrawingArea(height, width, pixels);
    }

    @Override
    public Bitmap getImage() {
        if (image == null || image.getWidth() != width || image.getHeight() != height) {
            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        image.setPixels(pixels, 0, width, 0, 0, width, height);
        return image;
    }

    @Override
    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public Canvas getCanvas() {
        return component;
    }
}
