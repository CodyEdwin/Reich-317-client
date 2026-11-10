package com.elvarg.android.geom;

/**
 * Android-neutral stand-in for {@code java.awt.Polygon}. Implements the subset
 * of the AWT API that the client and RuneLite overlays use: {@link #addPoint},
 * {@link #contains} (even-odd rule), {@link #getBounds} and
 * {@link #intersects}.
 */
public class Polygon implements Shape {

    public int[] xpoints;
    public int[] ypoints;
    public int npoints;

    private Rectangle bounds;

    public Polygon() {
        this(new int[4], new int[4], 0);
    }

    public Polygon(int[] xpoints, int[] ypoints, int npoints) {
        this.xpoints = xpoints;
        this.ypoints = ypoints;
        this.npoints = npoints;
    }

    public void addPoint(int x, int y) {
        if (npoints >= xpoints.length) {
            int newLength = Math.max(xpoints.length * 2, 4);
            int[] newX = new int[newLength];
            int[] newY = new int[newLength];
            System.arraycopy(xpoints, 0, newX, 0, npoints);
            System.arraycopy(ypoints, 0, newY, 0, npoints);
            xpoints = newX;
            ypoints = newY;
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        bounds = null;
    }

    public void reset() {
        npoints = 0;
        bounds = null;
    }

    @Override
    public Rectangle getBounds() {
        if (bounds == null) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (int i = 0; i < npoints; i++) {
                minX = Math.min(minX, xpoints[i]);
                minY = Math.min(minY, ypoints[i]);
                maxX = Math.max(maxX, xpoints[i]);
                maxY = Math.max(maxY, ypoints[i]);
            }
            bounds = npoints == 0
                    ? new Rectangle()
                    : new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        return new Rectangle(bounds);
    }

    @Override
    public boolean contains(int x, int y) {
        if (npoints < 3) {
            return false;
        }
        boolean inside = false;
        for (int i = 0, j = npoints - 1; i < npoints; j = i++) {
            int xi = xpoints[i], yi = ypoints[i];
            int xj = xpoints[j], yj = ypoints[j];
            if (((yi > y) != (yj > y)) && (x < (double) (xj - xi) * (y - yi) / (double) (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }

    @Override
    public boolean intersects(Rectangle r) {
        return getBounds().intersects(r);
    }
}
