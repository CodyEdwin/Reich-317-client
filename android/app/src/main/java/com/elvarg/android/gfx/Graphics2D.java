package com.elvarg.android.gfx;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.elvarg.android.geom.Polygon;
import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.geom.Shape;

/**
 * Android-neutral stand-in for {@code java.awt.Graphics2D}, wrapping an Android
 * {@link Canvas} + {@link Paint}.
 *
 * <p>Two variants exist, mirroring the two ways the desktop client obtained a
 * {@code Graphics2D}:
 *
 * <ul>
 *   <li><b>Overlay variant</b> ({@link #Graphics2D(Canvas, Paint, Bitmap, int[])}):
 *       used by {@code AndroidGraphics} to paint RuneLite overlays on top of the
 *       finished frame. The caller draws everything in one go and
 *       {@link #dispose()} copies the scratch bitmap back into the framebuffer.</li>
 *   <li><b>Buffer variant</b> ({@code flushPerOp}): used by
 *       {@code Rasterizer2D.createGraphics}. On desktop that graphics wrote
 *       into the client's pixel array <em>immediately</em>, because the AWT
 *       {@code BufferedImage} shared the {@code int[]}. Here the pixel array is
 *       mirrored into a scratch {@link Bitmap} (which is what {@link Canvas} can
 *       draw into), and after <em>every</em> drawing operation the affected
 *       region is copied back into the array. That keeps the desktop's
 *       interleaving of {@code Graphics2D} drawing (skill-orb rings, the world
 *       map marker) with direct {@code Rasterizer2D}/sprite drawing correct.
 *       {@link #dispose()} is a no-op for this variant.</li>
 * </ul>
 *
 * <p>Blending matches the desktop {@code DirectColorModel} image: the scratch
 * copy is made opaque (alpha forced to {@code 0xFF}) so translucent {@code Paint}
 * blends against the frame, and the alpha byte is stripped again on write-back
 * so the client's pixel convention ({@code 0x00RRGGBB}) is preserved.
 */
public final class Graphics2D {

    private final Canvas canvas;
    private final Paint paint;

    /** Non-null for buffer/overlay-backed instances. */
    private final Bitmap bitmap;
    /** Receives the pixels: the client's array, or nothing for the overlay case. */
    private final int[] target;
    private final boolean flushPerOp;
    private final boolean bufferBacked;

    /** Set once the canvas transform is non-identity: regions become the full frame. */
    private boolean transformed;

    public Graphics2D(Canvas canvas, Paint paint) {
        this(canvas, paint, null, null, false);
    }

    /** Overlay variant: the whole scratch bitmap is written back on {@link #dispose()}. */
    public Graphics2D(Canvas canvas, Paint paint, Bitmap bitmap, int[] target) {
        this(canvas, paint, bitmap, target, false);
    }

    public Graphics2D(Canvas canvas, Paint paint, Bitmap bitmap, int[] target, boolean flushPerOp) {
        this.canvas = canvas;
        this.paint = paint;
        this.bitmap = bitmap;
        this.target = target;
        this.flushPerOp = flushPerOp;
        this.bufferBacked = bitmap != null && target != null;
    }

    public Canvas canvas() {
        return canvas;
    }

    public Paint paint() {
        return paint;
    }

    public FontMetrics getFontMetrics() {
        return new FontMetrics(paint);
    }

    public void setColor(int argb) {
        paint.setColor(argb);
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setTextSize(float size) {
        paint.setTextSize(size);
    }

    public void setAntiAlias(boolean aa) {
        paint.setAntiAlias(aa);
    }

    /** Sets the paint's alpha (0-255), mirroring AWT's {@code setComposite}. */
    public void setAlpha(int alpha) {
        paint.setAlpha(Math.max(0, Math.min(255, alpha)));
    }

    public void drawString(String text, int x, int y) {
        int size = (int) paint.getTextSize() + 2;
        int left = x - 1;
        int top = y - size;
        int right = x + (int) paint.measureText(text) + 2;
        int bottom = y + size;
        syncIn(left, top, right, bottom);
        canvas.drawText(text, x, y, paint);
        syncOut(left, top, right, bottom);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        int left = Math.min(x1, x2) - 2;
        int top = Math.min(y1, y2) - 2;
        int right = Math.max(x1, x2) + 2;
        int bottom = Math.max(y1, y2) + 2;
        syncIn(left, top, right, bottom);
        canvas.drawLine(x1, y1, x2, y2, paint);
        syncOut(left, top, right, bottom);
    }

    public void drawRect(int x, int y, int w, int h) {
        syncIn(x - 2, y - 2, x + w + 2, y + h + 2);
        canvas.drawRect(x, y, x + w, y + h, paint);
        syncOut(x - 2, y - 2, x + w + 2, y + h + 2);
    }

    public void fillRect(int x, int y, int w, int h) {
        Paint.Style old = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);
        syncIn(x, y, x + w, y + h);
        canvas.drawRect(x, y, x + w, y + h, paint);
        syncOut(x, y, x + w, y + h);
        paint.setStyle(old);
    }

    public void rotate(double radians, float px, float py) {
        canvas.rotate((float) Math.toDegrees(radians), px, py);
        transformed = true;
    }

    public void translate(int x, int y) {
        canvas.translate(x, y);
        transformed = true;
    }

    public void draw(Shape shape) {
        Paint.Style old = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        drawShape(shape, true);
        paint.setStyle(old);
    }

    public void fill(Shape shape) {
        Paint.Style old = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);
        drawShape(shape, false);
        paint.setStyle(old);
    }

    public void drawPolygon(Polygon polygon) {
        draw(polygon);
    }

    public void fillPolygon(Polygon polygon) {
        fill(polygon);
    }

    public void fillPolygon(int[] xs, int[] ys, int n) {
        fill(new Polygon(xs, ys, n));
    }

    public void drawImage(Bitmap image, int x, int y) {
        syncIn(x, y, x + image.getWidth(), y + image.getHeight());
        canvas.drawBitmap(image, x, y, paint);
        syncOut(x, y, x + image.getWidth(), y + image.getHeight());
    }

    public void drawImage(Bitmap image, int x, int y, int w, int h, Object observer) {
        syncIn(x, y, x + w, y + h);
        canvas.drawBitmap(image, null, new Rect(x, y, x + w, y + h), paint);
        syncOut(x, y, x + w, y + h);
    }

    public void clipRect(int x, int y, int w, int h) {
        canvas.clipRect(x, y, x + w, y + h);
    }

    public void setClip(int x, int y, int w, int h) {
        canvas.save();
        canvas.clipRect(x, y, x + w, y + h);
    }

    public Rectangle getClipBounds() {
        android.graphics.Rect r = canvas.getClipBounds();
        return new Rectangle(r.left, r.top, r.width(), r.height());
    }

    /**
     * For the overlay variant, copies the scratch bitmap back into the target
     * array. For the buffer variant this is a no-op: every operation has already
     * flushed its own region, so flushing the whole bitmap here would overwrite
     * anything the client drew directly in the meantime.
     */
    public void dispose() {
        if (!bufferBacked || flushPerOp) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] buf = new int[width * height];
        bitmap.getPixels(buf, 0, width, 0, 0, width, height);
        int count = Math.min(buf.length, target.length);
        for (int i = 0; i < count; i++) {
            target[i] = buf[i] & 0x00FFFFFF;
        }
    }

    private void drawShape(Shape shape, boolean expanded) {
        Rectangle bounds = shape.getBounds();
        int pad = expanded ? 2 : 1;
        int left = bounds.x - pad;
        int top = bounds.y - pad;
        int right = bounds.x + bounds.width + pad;
        int bottom = bounds.y + bounds.height + pad;
        syncIn(left, top, right, bottom);
        canvas.drawPath(toPath(shape), paint);
        syncOut(left, top, right, bottom);
    }

    // ---- target <-> scratch bitmap synchronisation ------------------------

    private void syncIn(int left, int top, int right, int bottom) {
        if (!bufferBacked || !flushPerOp) {
            return;
        }
        int[] region = region(left, top, right, bottom);
        if (region == null) {
            return;
        }
        int l = region[0];
        int t = region[1];
        int rw = region[2] - l;
        int rh = region[3] - t;
        int stride = bitmap.getWidth();
        int[] buf = new int[rw * rh];
        for (int y = 0; y < rh; y++) {
            System.arraycopy(target, (t + y) * stride + l, buf, y * rw, rw);
        }
        for (int i = 0; i < buf.length; i++) {
            buf[i] |= 0xFF000000;
        }
        bitmap.setPixels(buf, 0, rw, l, t, rw, rh);
    }

    private void syncOut(int left, int top, int right, int bottom) {
        if (!bufferBacked || !flushPerOp) {
            return;
        }
        int[] region = region(left, top, right, bottom);
        if (region == null) {
            return;
        }
        int l = region[0];
        int t = region[1];
        int rw = region[2] - l;
        int rh = region[3] - t;
        int stride = bitmap.getWidth();
        int[] buf = new int[rw * rh];
        bitmap.getPixels(buf, 0, rw, l, t, rw, rh);
        for (int i = 0; i < buf.length; i++) {
            buf[i] &= 0x00FFFFFF;
        }
        for (int y = 0; y < rh; y++) {
            System.arraycopy(buf, y * rw, target, (t + y) * stride + l, rw);
        }
    }

    /** Clamps the region to the frame, widening it to the whole frame after a transform. */
    private int[] region(int left, int top, int right, int bottom) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (transformed) {
            return new int[]{0, 0, width, height};
        }
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(width, right);
        bottom = Math.min(height, bottom);
        if (right <= left || bottom <= top) {
            return null;
        }
        return new int[]{left, top, right, bottom};
    }

    private static Path toPath(Shape shape) {
        if (shape instanceof PathShape) {
            return new Path(((PathShape) shape).path());
        }
        Path path = new Path();
        if (shape instanceof Polygon) {
            Polygon p = (Polygon) shape;
            if (p.npoints > 0) {
                path.moveTo(p.xpoints[0], p.ypoints[0]);
                for (int i = 1; i < p.npoints; i++) {
                    path.lineTo(p.xpoints[i], p.ypoints[i]);
                }
                path.close();
            }
        } else {
            Rectangle b = shape.getBounds();
            path.addRect(b.x, b.y, b.x + b.width, b.y + b.height, Path.Direction.CW);
        }
        return path;
    }
}
