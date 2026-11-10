package com.elvarg.android.geom;

/**
 * Android-neutral stand-in for {@code java.awt.Rectangle}. Public mutable
 * {@code x}/{@code y}/{@code width}/{@code height} fields keep the same field
 * access pattern as the AWT type it replaces.
 */
public class Rectangle {

    public int x;
    public int y;
    public int width;
    public int height;

    public Rectangle() {
        this(0, 0, 0, 0);
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    public void add(int newx, int newy) {
        if (width < 0 || height < 0) {
            x = newx;
            y = newy;
            width = 0;
            height = 0;
            return;
        }
        int x1 = Math.min(x, newx);
        int y1 = Math.min(y, newy);
        int x2 = Math.max(x + width, newx);
        int y2 = Math.max(y + height, newy);
        x = x1;
        y = y1;
        width = x2 - x1;
        height = y2 - y1;
    }

    public boolean contains(int px, int py) {
        return width > 0 && height > 0
                && px >= x && py >= y && px < x + width && py < y + height;
    }

    public boolean intersects(Rectangle r) {
        return r.width > 0 && r.height > 0 && width > 0 && height > 0
                && r.x < x + width && r.x + r.width > x
                && r.y < y + height && r.y + r.height > y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMinX() {
        return x;
    }

    public double getMinY() {
        return y;
    }

    public double getMaxX() {
        return x + (double) width;
    }

    public double getMaxY() {
        return y + (double) height;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    public boolean isEmpty() {
        return width <= 0 || height <= 0;
    }

    /** AWT-compatible union: expands this rectangle to cover both. */
    public void union(Rectangle r) {
        if (r.width <= 0 || r.height <= 0) {
            return;
        }
        if (width <= 0 || height <= 0) {
            setBounds(r);
            return;
        }
        int x1 = Math.min(x, r.x);
        int y1 = Math.min(y, r.y);
        int x2 = Math.max(x + width, r.x + r.width);
        int y2 = Math.max(y + height, r.y + r.height);
        setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    /** AWT-compatible intersection: shrinks this rectangle to the overlap. */
    public void intersection(Rectangle r) {
        int x1 = Math.max(x, r.x);
        int y1 = Math.max(y, r.y);
        int x2 = Math.min(x + width, r.x + r.width);
        int y2 = Math.min(y + height, r.y + r.height);
        setBounds(x1, y1, Math.max(x2 - x1, 0), Math.max(y2 - y1, 0));
    }

    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

    @Override
    public String toString() {
        return "Rectangle[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }
}
