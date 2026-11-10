package com.elvarg.android.gfx;

import android.graphics.Paint;

/**
 * Android-neutral stand-in for {@code java.awt.FontMetrics}, backed by an
 * Android {@link Paint}. RuneLite's API measures text through
 * {@code Graphics2D.getFontMetrics()}; this keeps that call site working.
 */
public final class FontMetrics {

    private final Paint paint;

    public FontMetrics(Paint paint) {
        this.paint = paint;
    }

    public int stringWidth(String text) {
        if (text == null) {
            return 0;
        }
        return (int) Math.ceil(paint.measureText(text));
    }

    public int charWidth(char c) {
        return (int) Math.ceil(paint.measureText(String.valueOf(c)));
    }

    public int getHeight() {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        return fm.bottom - fm.top;
    }

    public int getAscent() {
        return -paint.getFontMetricsInt().ascent;
    }

    public int getDescent() {
        return paint.getFontMetricsInt().descent;
    }
}
