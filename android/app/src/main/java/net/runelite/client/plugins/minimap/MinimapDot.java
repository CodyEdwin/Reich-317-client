package net.runelite.client.plugins.minimap;

import net.runelite.api.Client;
import net.runelite.api.SpritePixels;

/**
 * Builds the minimap dot sprites.
 *
 * <p>Same 4x5 pixel template as the desktop plugin; the only difference is
 * that the colour is an ARGB int instead of a {@code java.awt.Color}.
 */
class MinimapDot {

    private static final int MAP_DOT_WIDTH = 4;
    private static final int MAP_DOT_HEIGHT = 5;

    private static int[] createPixels(int color) {
        int rgb = color | 0xFF000000;

        int[] pixels = new int[MAP_DOT_HEIGHT * MAP_DOT_WIDTH];

        pixels[0] = 0;
        pixels[1] = rgb;
        pixels[2] = rgb;
        pixels[3] = 0;

        pixels[4] = rgb;
        pixels[5] = rgb;
        pixels[6] = rgb;
        pixels[7] = rgb;

        pixels[8] = rgb;
        pixels[9] = rgb;
        pixels[10] = rgb;
        pixels[11] = rgb;

        pixels[12] = 1;
        pixels[13] = rgb;
        pixels[14] = rgb;
        pixels[15] = 1;

        pixels[16] = 0;
        pixels[17] = 1;
        pixels[18] = 1;
        pixels[19] = 0;

        return pixels;
    }

    static SpritePixels create(Client client, int color) {
        int[] pixels = createPixels(color);
        return client.createSpritePixels(pixels, MAP_DOT_WIDTH, MAP_DOT_HEIGHT);
    }
}
