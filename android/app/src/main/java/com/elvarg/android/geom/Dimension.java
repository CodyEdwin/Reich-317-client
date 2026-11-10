package com.elvarg.android.geom;

/**
 * Android-neutral stand-in for {@code java.awt.Dimension}.
 */
public class Dimension {

    public int width;
    public int height;

    public Dimension() {
        this(0, 0);
    }

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "Dimension[width=" + width + ",height=" + height + "]";
    }
}
