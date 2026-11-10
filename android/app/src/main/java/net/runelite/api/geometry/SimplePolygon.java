/*
 * Copyright (c) 2019 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.api.geometry;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Point;

import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.geom.Shape;

/**
 * Android port of RuneLite's {@link SimplePolygon}. The original implements
 * {@code java.awt.Shape}; here it implements {@link Shape} (the Android-neutral
 * equivalent) and drops the {@code PathIterator}/{@code AffineTransform}
 * plumbing, which Android does not need.
 *
 * <p>A simple list of vertices that can be appended or prepended to.
 */
public class SimplePolygon implements Shape {

    private static final int GROW = 16;

    protected int[] x;
    protected int[] y;
    protected int left;
    protected int right;

    public SimplePolygon() {
        this(new int[32], new int[32], 16, 15);
    }

    public SimplePolygon(int[] x, int[] y, int length) {
        this(x, y, 0, length - 1);
    }

    public SimplePolygon(int[] x, int[] y, int left, int right) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
    }

    public int[] getX() {
        return x;
    }

    public int[] getY() {
        return y;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public void pushLeft(int xCoord, int yCoord) {
        left--;
        if (left < 0) {
            expandLeft(GROW);
        }
        x[left] = xCoord;
        y[left] = yCoord;
    }

    public void popLeft() {
        left++;
    }

    protected void expandLeft(int grow) {
        int[] nx = new int[x.length + grow];
        System.arraycopy(x, 0, nx, grow, x.length);
        x = nx;
        int[] ny = new int[nx.length];
        System.arraycopy(y, 0, ny, grow, y.length);
        y = ny;
        left += grow;
        right += grow;
    }

    public void pushRight(int xCoord, int yCoord) {
        right++;
        if (right >= x.length) {
            expandRight(GROW);
        }
        x[right] = xCoord;
        y[right] = yCoord;
    }

    public void popRight() {
        right--;
    }

    protected void expandRight(int grow) {
        int[] nx = new int[x.length + grow];
        System.arraycopy(x, 0, nx, 0, x.length);
        x = nx;
        int[] ny = new int[nx.length];
        System.arraycopy(y, 0, ny, 0, y.length);
        y = ny;
    }

    public int getX(int index) {
        return x[left + index];
    }

    public int getY(int index) {
        return y[left + index];
    }

    public int size() {
        return right - left + 1;
    }

    public List<Point> toRuneLitePointList() {
        List<Point> out = new ArrayList<>(size());
        for (int i = left; i <= right; i++) {
            out.add(new Point(x[i], y[i]));
        }
        return out;
    }

    public void copyTo(int[] xDest, int[] yDest, int offset) {
        System.arraycopy(x, left, xDest, offset, size());
        System.arraycopy(y, left, yDest, offset, size());
    }

    public void appendTo(SimplePolygon other) {
        int size = size();
        if (size <= 0) {
            return;
        }
        other.expandRight(size);
        copyTo(other.x, other.y, other.right + 1);
        other.right += size;
    }

    public void reverse() {
        int half = size() / 2;
        for (int i = 0; i < half; i++) {
            int li = left + i;
            int ri = right - i;
            int tx = x[li];
            int ty = y[li];
            x[li] = x[ri];
            y[li] = y[ri];
            x[ri] = tx;
            y[ri] = ty;
        }
    }

    /**
     * Clips the polygon with the passed convex polygon
     */
    public void intersectWithConvex(SimplePolygon convex) {
        // Sutherland-Hodgman
        int[] tx = new int[size()];
        int[] ty = new int[tx.length];

        int cx1 = convex.x[convex.right];
        int cy1 = convex.y[convex.right];
        for (int ci = convex.left; ci <= convex.right; ci++) {
            if (size() < 3) {
                return;
            }

            int tRight = this.right;
            int tLeft = this.left;

            int[] tmpX = x;
            int[] tmpY = y;

            this.x = tx;
            this.y = ty;
            this.left = 0;
            this.right = -1;
            tx = tmpX;
            ty = tmpY;

            int cx2 = convex.x[ci];
            int cy2 = convex.y[ci];

            int tx1 = tx[tRight];
            int ty1 = ty[tRight];

            for (int ti = tLeft; ti <= tRight; ti++) {
                int tx2 = tx[ti];
                int ty2 = ty[ti];

                int p1 = (cx2 - cx1) * (ty1 - cy1) - (cy2 - cy1) * (tx1 - cx1);
                int p2 = (cx2 - cx1) * (ty2 - cy1) - (cy2 - cy1) * (tx2 - cx1);

                if (p1 < 0 && p2 < 0) {
                    pushRight(tx2, ty2);
                } else if (p1 >= 0 != p2 >= 0) {
                    long nota = cx1 * (long) cy2 - cy1 * (long) cx2;
                    long clue = tx1 * (long) ty2 - ty1 * (long) tx2;
                    long div = ((cx1 - cx2) * (long) (ty1 - ty2) - (cy1 - cy2) * (long) (tx1 - tx2));
                    pushRight((int) ((nota * (tx1 - tx2) - (cx1 - cx2) * clue) / div),
                            (int) ((nota * (ty1 - ty2) - (cy1 - cy2) * clue) / div));

                    if (p1 >= 0) {
                        pushRight(tx2, ty2);
                    }
                }

                tx1 = tx2;
                ty1 = ty2;
            }

            cx1 = cx2;
            cy1 = cy2;
        }
    }

    @Override
    public Rectangle getBounds() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = left; i <= right; i++) {
            final int xs = x[i];
            final int ys = y[i];

            if (xs < minX) {
                minX = xs;
            }
            if (xs > maxX) {
                maxX = xs;
            }
            if (ys < minY) {
                minY = ys;
            }
            if (ys > maxY) {
                maxY = ys;
            }
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public boolean contains(int cx, int cy) {
        if (size() < 3) {
            return false;
        }

        return (crossings(cx, cy, false) & 1) != 0;
    }

    private int crossings(double cx, double cy, boolean swap) {
        int collisions = 0;

        int[] x = this.x;
        int[] y = this.y;
        if (swap) {
            y = this.x;
            x = this.y;
        }

        for (int x0 = x[right], y0 = y[right], x1, y1, i = left; i <= right; i++, x0 = x1, y0 = y1) {
            x1 = x[i];
            y1 = y[i];

            if (y0 == y1) {
                continue;
            }

            double dy0 = y0, dy1 = y1;

            if (cy <= dy0 == cy <= dy1) {
                continue;
            }

            double dx0 = x0, dx1 = x1;

            boolean left = cx < dx0;
            if (left == cx < dx1) {
                if (!left) {
                    collisions++;
                }
                continue;
            }

            if ((dx1 - dx0) * (cy - dy0) - (cx - dx0) * (dy1 - dy0) > 0 == dy0 > dy1) {
                collisions++;
            }
        }
        return collisions;
    }

    @Override
    public boolean intersects(Rectangle r) {
        double x0 = r.x;
        double y0 = r.y;
        double w = r.width;
        double h = r.height;

        double x1 = x0 + w;
        double y1 = y0 + h;

        return crossings(x0, y0, false) != crossings(x1, y0, false) // top
                || crossings(x0, y1, false) != crossings(x1, y1, false) // bottom
                || crossings(x0, y0, true) != crossings(x0, y1, true) // left
                || crossings(x1, y0, true) != crossings(x1, y1, true); // right
    }
}
