package com.runescape.draw;

import com.runescape.Client;
import com.runescape.collection.Cacheable;
import net.runelite.rs.api.RSRasterizer2D;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.elvarg.android.geom.Shape;
import com.elvarg.android.gfx.Graphics2D;
import com.elvarg.android.gfx.PathShape;

public class Rasterizer2D extends Cacheable implements RSRasterizer2D {

    public static void drawAlpha(int[] pixels, int index, int value, int alpha) {
        if (! Client.instance.isGpu() || pixels != Client.instance.getBufferProvider().getPixels())
        {
            pixels[index] = value;
            return;
        }

        // (int) x * 0x8081 >>> 23 is equivalent to (short) x / 255
        int outAlpha = alpha + ((pixels[index] >>> 24) * (255 - alpha) * 0x8081 >>> 23);
        pixels[index] = value & 0x00FFFFFF | outAlpha << 24;
    }

    /**
     * Sets the Rasterizer2D in the upper left corner with height, width and pixels set.
     * @param height The height of the drawingArea.
     * @param width The width of the drawingArea.
     * @param pixels The array of pixels (RGBColours) in the drawingArea.
     */
    public static void initDrawingArea(int height, int width, int pixels[]) {
        Rasterizer2D.pixels = pixels;
        Rasterizer2D.width = width;
        Rasterizer2D.height = height;
        setDrawingArea(height, 0, width, 0);
    }

    /**
     * Draws a transparent box with a gradient that changes from top to bottom.
     * @param leftX The left edge X-Coordinate of the box.
     * @param topY The top edge Y-Coordinate of the box.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param topColour The top rgbColour of the gradient.
     * @param bottomColour The bottom rgbColour of the gradient.
     * @param opacity The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentGradientBox(int leftX, int topY, int width, int height, int topColour, int bottomColour, int opacity) {
        int gradientProgress = 0;
        int progressPerPixel = 0x10000 / height;
        if(leftX < Rasterizer2D.leftX) {
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if(topY < Rasterizer2D.topY) {
            gradientProgress += (Rasterizer2D.topY - topY) * progressPerPixel;
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if(leftX + width > bottomX)
            width = bottomX - leftX;
        if(topY + height > bottomY)
            height = bottomY - topY;
        int leftOver = Rasterizer2D.width - width;
        int transparency = 256 - opacity;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for(int rowIndex = 0; rowIndex < height; rowIndex++) {
            int gradient = 0x10000 - gradientProgress >> 8;
            int inverseGradient = gradientProgress >> 8;
            int gradientColour = ((topColour & 0xff00ff) * gradient + (bottomColour & 0xff00ff) * inverseGradient & 0xff00ff00) + ((topColour & 0xff00) * gradient + (bottomColour & 0xff00) * inverseGradient & 0xff0000) >>> 8;
            int transparentPixel = ((gradientColour & 0xff00ff) * opacity >> 8 & 0xff00ff) + ((gradientColour & 0xff00) * opacity >> 8 & 0xff00);
            for(int columnIndex = 0; columnIndex < width; columnIndex++) {
                int backgroundPixel = pixels[pixelIndex];
                backgroundPixel = ((backgroundPixel & 0xff00ff) * transparency >> 8 & 0xff00ff) + ((backgroundPixel & 0xff00) * transparency >> 8 & 0xff00);
                drawAlpha(pixels, pixelIndex++, transparentPixel + backgroundPixel, opacity);
            }
            pixelIndex += leftOver;
            gradientProgress += progressPerPixel;
        }
    }

    /**
     * Sets the drawingArea based on the coordinates of the edges.
     * @param bottomY The bottom edge Y-Coordinate.
     * @param leftX The left edge X-Coordinate.
     * @param rightX The right edge X-Coordinate.
     * @param topY The top edge Y-Coordinate.
     */
    public static void setDrawingArea(int bottomY, int leftX, int rightX, int topY) {
        if(leftX < 0) {
            leftX = 0;
        }
        if(topY < 0) {
            topY = 0;
        }
        if(rightX > width) {
            rightX = width;
        }
        if(bottomY > height) {
            bottomY = height;
        }
        Rasterizer2D.leftX = leftX;
        Rasterizer2D.topY = topY;
        bottomX = rightX;
        Rasterizer2D.bottomY = bottomY;
        lastX = bottomX;
        viewportCenterX = bottomX / 2;
        viewportCenterY = Rasterizer2D.bottomY / 2;
    }

    /**
     * Clears the drawingArea by setting every pixel to 0 (black).
     */
    public static void clear()	{
        int i = width * height;
        for(int j = 0; j < i; j++) {
            pixels[j] = 0;
        }
    }

    /**
     * Draws a box filled with a certain colour.
     * @param leftX The left edge X-Coordinate of the box.
     * @param topY The top edge Y-Coordinate of the box.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param rgbColour The RGBColour of the box.
     */
    public static void drawBox(int leftX, int topY, int width, int height, int rgbColour) {
        if (leftX < Rasterizer2D.leftX) {
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if (topY < Rasterizer2D.topY) {
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if (leftX + width > bottomX)
            width = bottomX - leftX;
        if (topY + height > bottomY)
            height = bottomY - topY;
        int leftOver = Rasterizer2D.width - width;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++)
                drawAlpha(pixels, pixelIndex++, rgbColour, 255);
            pixelIndex += leftOver;
        }
    }

    /**
     * Draws an item box filled with a certain colour.
     *
     * @param leftX     The left edge X-Coordinate of the box.
     * @param topY      The top edge Y-Coordinate of the box.
     * @param width     The width of the box.
     * @param height    The height of the box.
     * @param rgbColour The RGBColour of the box.
     */
    public static void drawItemBox(int leftX, int topY, int width, int height, int rgbColour) {
        if (leftX < Rasterizer2D.leftX) {
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if (topY < Rasterizer2D.topY) {
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if (leftX + width > bottomX)
            width = bottomX - leftX;
        if (topY + height > bottomY)
            height = bottomY - topY;
        int leftOver = Rasterizer2D.width - width;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++)
                //pixels[pixelIndex++] = rgbColour;
                drawAlpha(pixels, pixelIndex++, rgbColour, 0);
            pixelIndex += leftOver;
        }
    }

    /**
     * Draws a transparent box.
     * @param leftX The left edge X-Coordinate of the box.
     * @param topY The top edge Y-Coordinate of the box.
     * @param width The box width.
     * @param height The box height.
     * @param rgbColour The box colour.
     * @param opacity The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentBox(int leftX, int topY, int width, int height, int rgbColour, int opacity){
        if(leftX < Rasterizer2D.leftX){
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if(topY < Rasterizer2D.topY){
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if(leftX + width > bottomX)
            width = bottomX - leftX;
        if(topY + height > bottomY)
            height = bottomY - topY;
        int transparency = 256 - opacity;
        int red = (rgbColour >> 16 & 0xff) * opacity;
        int green = (rgbColour >> 8 & 0xff) * opacity;
        int blue = (rgbColour & 0xff) * opacity;
        int leftOver = Rasterizer2D.width - width;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for(int rowIndex = 0; rowIndex < height; rowIndex++){
            for(int columnIndex = 0; columnIndex < width; columnIndex++){
                int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
                int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
                int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
                int transparentColour = ((red + otherRed >> 8) << 16) + ((green + otherGreen >> 8) << 8) + (blue + otherBlue >> 8);
                drawAlpha(pixels, pixelIndex++, transparentColour, opacity);
            }
            pixelIndex += leftOver;
        }
    }

    public static void drawPixels(int height, int posY, int posX, int color, int width) {
        if (posX < leftX) {
            width -= leftX - posX;
            posX = leftX;
        }
        if (posY < topY) {
            height -= topY - posY;
            posY = topY;
        }
        if (posX + width > bottomX) {
            width = bottomX - posX;
        }
        if (posY + height > bottomY) {
            height = bottomY - posY;
        }
        int k1 = Rasterizer2D.width - width;
        int l1 = posX + posY * Rasterizer2D.width;
        for (int i2 = -height; i2 < 0; i2++) {
            for (int j2 = -width; j2 < 0; j2++) {
                pixels[l1++] = color;
            }
            l1 += k1;
        }
    }

    /**
     * Draws a 1 pixel thick box outline in a certain colour.
     * @param leftX The left edge X-Coordinate.
     * @param topY The top edge Y-Coordinate.
     * @param width The width.
     * @param height The height.
     * @param rgbColour The RGB-Colour.
     */
    public static void drawBoxOutline(int leftX, int topY, int width, int height, int rgbColour){
        drawHorizontalLine(leftX, topY, width, rgbColour);
        drawHorizontalLine(leftX, (topY + height) - 1, width, rgbColour);
        drawVerticalLine(leftX, topY, height, rgbColour);
        drawVerticalLine((leftX + width) - 1, topY, height, rgbColour);
    }

    /**
     * Draws a coloured horizontal line in the drawingArea.
     * @param xPosition The start X-Position of the line.
     * @param yPosition The Y-Position of the line.
     * @param width The width of the line.
     * @param rgbColour The colour of the line.
     */
    public static void drawHorizontalLine(int xPosition, int yPosition, int width, int rgbColour){
        if(yPosition < topY || yPosition >= bottomY)
            return;
        if(xPosition < leftX){
            width -= leftX - xPosition;
            xPosition = leftX;
        }
        if(xPosition + width > bottomX)
            width = bottomX - xPosition;
        int pixelIndex = xPosition + yPosition * Rasterizer2D.width;
        for(int i = 0; i < width; i++)
            drawAlpha(pixels, pixelIndex + i, rgbColour, 255);
    }

    /**
     * Draws a coloured vertical line in the drawingArea.
     * @param xPosition The X-Position of the line.
     * @param yPosition The start Y-Position of the line.
     * @param height The height of the line.
     * @param rgbColour The colour of the line.
     */
    public static void drawVerticalLine(int xPosition, int yPosition, int height, int rgbColour){
        if(xPosition < leftX || xPosition >= bottomX)
            return;
        if(yPosition < topY){
            height -= topY - yPosition;
            yPosition = topY;
        }
        if(yPosition + height > bottomY)
            height = bottomY - yPosition;
        int pixelIndex = xPosition + yPosition * width;
        for(int rowIndex = 0; rowIndex < height; rowIndex++)
            drawAlpha(pixels, pixelIndex + rowIndex * width, rgbColour, 255);
    }

    /**
     * Draws a 1 pixel thick transparent box outline in a certain colour.
     * @param leftX The left edge X-Coordinate
     * @param topY The top edge Y-Coordinate.
     * @param width The width.
     * @param height The height.
     * @param rgbColour The RGB-Colour.
     * @param opacity The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentBoxOutline(int leftX, int topY, int width, int height, int rgbColour, int opacity) {
        drawTransparentHorizontalLine(leftX, topY, width, rgbColour, opacity);
        drawTransparentHorizontalLine(leftX, topY + height - 1, width, rgbColour, opacity);
        if(height >= 3) {
            drawTransparentVerticalLine(leftX, topY + 1, height - 2, rgbColour, opacity);
            drawTransparentVerticalLine(leftX + width - 1, topY + 1, height - 2, rgbColour, opacity);
        }
    }

    /**
     * Draws a transparent coloured horizontal line in the drawingArea.
     * @param xPosition The start X-Position of the line.
     * @param yPosition The Y-Position of the line.
     * @param width The width of the line.
     * @param rgbColour The colour of the line.
     * @param opacity The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentHorizontalLine(int xPosition, int yPosition, int width, int rgbColour, int opacity) {
        if(yPosition < topY || yPosition >= bottomY) {
            return;
        }
        if(xPosition < leftX) {
            width -= leftX - xPosition;
            xPosition = leftX;
        }
        if(xPosition + width > bottomX) {
            width = bottomX - xPosition;
        }
        final int transparency = 256 - opacity;
        final int red = (rgbColour >> 16 & 0xff) * opacity;
        final int green = (rgbColour >> 8 & 0xff) * opacity;
        final int blue = (rgbColour & 0xff) * opacity;
        int pixelIndex = xPosition + yPosition * Rasterizer2D.width;
        for(int i = 0; i < width; i++) {
            final int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
            final int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
            final int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
            final int transparentColour = (red + otherRed >> 8 << 16) + (green + otherGreen >> 8 << 8) + (blue + otherBlue >> 8);
            drawAlpha(pixels, pixelIndex, transparentColour, opacity);
        }
    }

    /**
     * Draws a transparent coloured vertical line in the drawingArea.
     * @param xPosition The X-Position of the line.
     * @param yPosition The start Y-Position of the line.
     * @param height The height of the line.
     * @param rgbColour The colour of the line.
     * @param opacity The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentVerticalLine(int xPosition, int yPosition, int height, int rgbColour, int opacity) {
        if(xPosition < leftX || xPosition >= bottomX) {
            return;
        }
        if(yPosition < topY) {
            height -= topY - yPosition;
            yPosition = topY;
        }
        if(yPosition + height > bottomY) {
            height = bottomY - yPosition;
        }
        final int transparency = 256 - opacity;
        final int red = (rgbColour >> 16 & 0xff) * opacity;
        final int green = (rgbColour >> 8 & 0xff) * opacity;
        final int blue = (rgbColour & 0xff) * opacity;
        int pixelIndex = xPosition + yPosition * width;
        for(int i = 0; i < height; i++) {
            final int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
            final int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
            final int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
            final int transparentColour = (red + otherRed >> 8 << 16) + (green + otherGreen >> 8 << 8) + (blue + otherBlue >> 8);
            drawAlpha(pixels, pixelIndex, transparentColour, opacity);
            pixelIndex += width;
        }
    }
    public static int pixels[];
    public static int width;
    public static int height;
    public static int topY;
    public static int bottomY;
    public static int leftX;
    public static int bottomX;
    public static int lastX;
    public static int viewportCenterX;
    public static int viewportCenterY;

    public static Graphics2D createGraphics(boolean renderingHints) {
        return createGraphics(pixels, width, height, renderingHints);
    }

    /**
     * Returns a {@link Graphics2D} that draws into a copy of {@code pixels} and
     * keeps that array up to date after every drawing operation. This is the
     * Android stand-in for the desktop's {@code BufferedImage}-backed graphics,
     * which shared the client's pixel array and therefore took effect
     * immediately; the per-operation flush preserves that interleaving with the
     * direct-to-array {@code Rasterizer2D} and sprite drawing.
     */
    public static Graphics2D createGraphics(int[] pixels, int width, int height) {
        return createGraphics(pixels, width, height, true);
    }

    /**
     * Scratch bitmap/pixels for {@link #createGraphics}; reused across calls
     * because skill orbs request a graphics context every frame and allocating
     * a full-screen bitmap each time causes heavy GC churn on a device.
     */
    private static Bitmap graphicsBitmap;
    private static int[] graphicsPixels;
    private static int graphicsWidth;
    private static int graphicsHeight;

    public static Graphics2D createGraphics(int[] pixels, int width, int height, boolean renderingHints) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (graphicsBitmap == null || graphicsWidth != w || graphicsHeight != h) {
            graphicsBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            graphicsPixels = new int[w * h];
            graphicsWidth = w;
            graphicsHeight = h;
        }
        if (pixels != null) {
            int count = Math.min(graphicsPixels.length, pixels.length);
            System.arraycopy(pixels, 0, graphicsPixels, 0, count);
            for (int i = 0; i < count; i++) {
                graphicsPixels[i] |= 0xFF000000;
            }
            graphicsBitmap.setPixels(graphicsPixels, 0, w, 0, 0, w, h);
        }
        // A fresh Canvas per call: Canvas keeps its clip/transform, so reusing
        // it would leak state from one graphics context into the next.
        Canvas canvas = new Canvas(graphicsBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(renderingHints);
        return new Graphics2D(canvas, paint, graphicsBitmap, pixels, true);
    }

    /**
     * Android port of the AWT {@code Arc2D.PIE} sector. AWT measures 90 degrees
     * at 12 o'clock and a negative extent clockwise; Android's path arcs start
     * at 3 o'clock and sweep clockwise, so the start becomes -90 and the sweep
     * keeps the same magnitude.
     */
    public static Shape createSector(int x, int y, int r, int angle) {
        Path path = new Path();
        float cx = x + r / 2f;
        float cy = y + r / 2f;
        RectF oval = new RectF(x, y, x + r, y + r);
        path.moveTo(cx, cy);
        path.arcTo(oval, -90f, angle, false);
        path.close();
        return new PathShape(path);
    }

    /** Android port of the AWT {@code Ellipse2D} inscribed in (x, y, x+r, y+r). */
    public static Shape createCircle(int x, int y, int r) {
        Path path = new Path();
        path.addCircle(x + r / 2f, y + r / 2f, r / 2f, Path.Direction.CW);
        return new PathShape(path);
    }

    public static void drawFilledCircle(int x, int y, int radius, int color, int alpha) {
        int y1 = y - radius;
        if (y1 < 0) {
            y1 = 0;
        }
        int y2 = y + radius;
        if (y2 >= height) {
            y2 = height - 1;
        }
        int a2 = 256 - alpha;
        int r1 = (color >> 16 & 0xff) * alpha;
        int g1 = (color >> 8 & 0xff) * alpha;
        int b1 = (color & 0xff) * alpha;
        for (int iy = y1; iy <= y2; iy++) {
            int dy = iy - y;
            int dist = (int) Math.sqrt(radius * radius - dy * dy);
            int x1 = x - dist;
            if (x1 < 0) {
                x1 = 0;
            }
            int x2 = x + dist;
            if (x2 >= width) {
                x2 = width - 1;
            }
            int pos = x1 + iy * width;
            for (int ix = x1; ix <= x2; ix++) {
                int r2 = (pixels[pos] >> 16 & 0xff) * a2;
                int g2 = (pixels[pos] >> 8 & 0xff) * a2;
                int b2 = (pixels[pos] & 0xff) * a2;
                drawAlpha(pixels, pos++, ((r1 + r2 >> 8) << 16) + ((g1 + g2 >> 8) << 8) + (b1 + b2 >> 8), alpha);

            }
        }
    }

    public static Shape createRing(Shape sector, Shape innerCircle) {
        Path ring = new Path();
        ring.setFillType(Path.FillType.EVEN_ODD);
        if (sector instanceof PathShape) {
            ring.addPath(((PathShape) sector).path());
        }
        if (innerCircle instanceof PathShape) {
            ring.addPath(((PathShape) innerCircle).path());
        }
        return new PathShape(ring);
    }

}