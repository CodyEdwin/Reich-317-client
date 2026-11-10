package com.runescape.util;

import com.runescape.sign.SignLink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Android port of {@code ImagePacker}.
 *
 * <p>The desktop helper repacked PNG/JPG files from {@code index6} into the
 * sprite cache using {@code ImageIO}. Android has no {@code ImageIO}, and the
 * repack is disabled ({@code REPACK_SPRITES == false}) because the shipped cache
 * is already packed, so this port keeps the guard and the gzip helper but drops
 * the image codec. Re-enabling it on Android would use
 * {@code android.graphics.BitmapFactory}/{@code Bitmap#compress}.
 *
 * <p>Written in Java (the desktop original is a Kotlin {@code object}); the
 * {@code INSTANCE} field keeps the call sites identical.
 */
public final class ImagePacker {

    private static final boolean REPACK_SPRITES = false;

    private static final List<String> VALID_EXTENSIONS = Arrays.asList("png", "jpg");

    /** Mirrors the singleton accessor of the Kotlin {@code object}. */
    public static final ImagePacker INSTANCE = new ImagePacker();

    private ImagePacker() {
    }

    public void init() {
        if (!REPACK_SPRITES) {
            return;
        }

        // Image repacking needs a bitmap codec (ImageIO on desktop,
        // BitmapFactory/Bitmap.compress on Android); it is disabled for the
        // packaged cache, so nothing to do here.
        String imageLocation = SignLink.findcachedir() + "/index6/";
        System.out.println("Sprite repack disabled (REPACK_SPRITES=false); index6=" + imageLocation
                + "; extensions=" + VALID_EXTENSIONS);
    }

    private static byte[] toGzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bytes.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
            zipStream.write(bytes);
        }
        return byteStream.toByteArray();
    }
}
