package com.runescape.cache.graphics.sprite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import com.runescape.sign.SignLink;
import static java.nio.file.StandardOpenOption.READ;

/**
 * Android port of {@code SpriteCache}. The desktop version decoded each entry
 * with {@code ImageIO} into a {@code TYPE_INT_ARGB BufferedImage} and pulled the
 * {@code DataBufferInt} out of it; Android decodes with {@link BitmapFactory}
 * and reads the pixels back with {@link Bitmap#getPixels}. The cache/index file
 * format and lookup logic are unchanged.
 */
public final class SpriteCache implements Closeable {

    private Sprite[] cache;
    private FileChannel dataChannel;
    private FileChannel metaChannel;

    public void init() throws IOException {
        File dataFile = new File(SignLink.findcachedir() + "main_file_sprites.dat");
        File metaFile = new File(SignLink.findcachedir() + "main_file_sprites.idx");
        if (!dataFile.exists()) {
            throw new IOException(String.format("Could not find data file=%s", dataFile.getName()));
        }

        if (!metaFile.exists()) {
            throw new IOException(String.format("Could not find meta file=%s", metaFile.getName()));
        }

        dataChannel = FileChannel.open(dataFile.toPath(), READ);
        metaChannel = FileChannel.open(metaFile.toPath(), READ);

        final int spriteCount = Math.toIntExact(metaChannel.size() / 10);

        cache = new Sprite[spriteCount];

        System.out.println(String.format("Sprite cache size=%d", cache.length));
    }

    public Sprite lookup(int id) {
        try {
            if (contains(id)) {
                return cache[id];
            }

            if (!dataChannel.isOpen() || !metaChannel.isOpen()) {
                System.out.println("Sprite channels are closed!");
                return Sprite.EMPTY_SPRITE;
            }

            final int entries = Math.toIntExact(metaChannel.size() / 10);

            if (id > entries) {
                System.out.println(String.format("id=%d > size=%d", id, entries));
                return Sprite.EMPTY_SPRITE;
            }

            metaChannel.position(id * 10);

            final ByteBuffer metaBuf = ByteBuffer.allocate(10);
            metaChannel.read(metaBuf);
            metaBuf.flip();

            final int pos = ((metaBuf.get() & 0xFF) << 16) | ((metaBuf.get() & 0xFF) << 8) | (metaBuf.get() & 0xFF);
            final int len = ((metaBuf.get() & 0xFF) << 16) | ((metaBuf.get() & 0xFF) << 8) | (metaBuf.get() & 0xFF);
            final int offsetX = metaBuf.getShort() & 0xFF;
            final int offsetY = metaBuf.getShort() & 0xFF;

            final ByteBuffer dataBuf = ByteBuffer.allocate(len);

            dataChannel.position(pos);
            dataChannel.read(dataBuf);
            dataBuf.flip();

            final byte[] data = dataBuf.array();

            Bitmap bimage = BitmapFactory.decodeByteArray(data, 0, data.length);

            if (bimage == null) {
                System.out.println(String.format("Could not read image at %d", id));
                return Sprite.EMPTY_SPRITE;
            }

            final int width = bimage.getWidth();
            final int height = bimage.getHeight();
            final int[] pixels = new int[width * height];
            bimage.getPixels(pixels, 0, width, 0, 0, width, height);

            final Sprite sprite = new Sprite(width, height, offsetX, offsetY, pixels);

            // cache so we don't have to perform I/O calls again
            cache[id] = sprite;

            return sprite;
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(String.format("No sprite found for id=%d", id));
        return Sprite.EMPTY_SPRITE;
    }
    
    public void draw(int id, int x, int y) {
        draw(id, x, y, false);
    }
    
    public void draw(int id, int x, int y, int alpha, boolean advanced) {
        Sprite sprite = lookup(id);
        if (sprite != null) {
            if (advanced) {
                sprite.drawAdvancedSprite(x, y, alpha);
            } else {
                sprite.drawSprite(x, y, alpha);
            }
        }
    }
    
    public void draw(int id, int x, int y, boolean advanced) {
        Sprite sprite = lookup(id);
        if (sprite != null) {
            if (advanced) {
                sprite.drawAdvancedSprite(x, y);
            } else {
                sprite.drawSprite(x, y);
            }
        }
    }

    public boolean contains(int id) {
        return id < cache.length && cache[id] != null;
    }

    public void set(int id, Sprite sprite) {
        if (!contains(id)) {
            return;
        }

        cache[id] = sprite;
    }

    public void close() throws IOException {
        dataChannel.close();
        metaChannel.close();
    }

}
