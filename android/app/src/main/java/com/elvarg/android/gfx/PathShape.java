package com.elvarg.android.gfx;

import android.graphics.Path;
import android.graphics.RectF;

import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.geom.Shape;

/**
 * A {@link Shape} backed by an Android {@link Path}. Used to represent the
 * non-polygonal geometry (circle sectors, rings) that the client's skill-orb and
 * wheel-of-fortune rendering build instead of AWT's {@code Arc2D}/{@code Area}.
 */
public final class PathShape implements Shape {

    private final Path path;

    public PathShape() {
        this(new Path());
    }

    public PathShape(Path path) {
        this.path = path;
    }

    public Path path() {
        return path;
    }

    public void setFillTypeEvenOdd() {
        path.setFillType(Path.FillType.EVEN_ODD);
    }

    @Override
    public Rectangle getBounds() {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        return new Rectangle((int) Math.floor(bounds.left), (int) Math.floor(bounds.top),
                (int) Math.ceil(bounds.width()), (int) Math.ceil(bounds.height()));
    }

    @Override
    public boolean contains(int x, int y) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        return bounds.contains(x, y);
    }

    @Override
    public boolean intersects(Rectangle r) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        return RectF.intersects(bounds, new RectF(r.x, r.y, r.x + r.width, r.y + r.height));
    }
}
