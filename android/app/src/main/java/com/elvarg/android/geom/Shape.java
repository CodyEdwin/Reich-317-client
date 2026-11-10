package com.elvarg.android.geom;

/**
 * Android-neutral stand-in for {@code java.awt.Shape}.
 *
 * <p>The desktop RuneLite API exposes geometry to plugins through
 * {@code java.awt.Shape}/{@code Polygon}/{@code Rectangle}. Android cannot load
 * {@code java.awt.*}, so the ported API is re-typed against these types instead.
 */
public interface Shape {

    /** The integer bounding box of this shape. */
    Rectangle getBounds();

    /** Whether (x, y) is inside this shape. */
    boolean contains(int x, int y);

    /** Whether this shape intersects the given rectangle. */
    boolean intersects(Rectangle r);
}
