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

import java.util.Arrays;
import java.util.List;

import com.elvarg.android.geom.Rectangle;
import com.elvarg.android.geom.Shape;

/**
 * Android port of RuneLite's {@link Shapes}: a union of several shapes exposed
 * as a single {@link Shape}. Implements the Android-neutral geometry interface
 * instead of {@code java.awt.Shape}.
 */
public class Shapes<T extends Shape> implements Shape {

    public Shapes(T... shape) {
        this(Arrays.asList(shape));
    }

    public Shapes(List<T> shapes) {
        this.shapes = shapes;
    }

    private final List<T> shapes;

    public List<T> getShapes() {
        return shapes;
    }

    @Override
    public Rectangle getBounds() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Shape shape : shapes) {
            Rectangle bounds = shape.getBounds();
            minX = Math.min(bounds.x, minX);
            minY = Math.min(bounds.y, minY);
            maxX = Math.max(bounds.x + bounds.width, maxX);
            maxY = Math.max(bounds.y + bounds.height, maxY);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public boolean contains(int x, int y) {
        return shapes.stream().anyMatch(s -> s.contains(x, y));
    }

    @Override
    public boolean intersects(Rectangle r) {
        return shapes.stream().anyMatch(s -> s.intersects(r));
    }
}
