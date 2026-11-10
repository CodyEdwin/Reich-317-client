package com.elvarg.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Saves the game framebuffer as a PNG.
 *
 * <p>On Android 10 and newer the picture goes into the shared Pictures
 * collection; on older versions it is written into the app's own pictures
 * directory (no storage permission needed either way).
 */
public final class Screenshot {

    private Screenshot() {
    }

    /**
     * Writes the bitmap to the device and returns a description of where it
     * went (a file name or a content uri).
     */
    public static String save(Context context, Bitmap bitmap) {
        String name = "elvarg-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + ".png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "Elvarg");
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return null;
            }
            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Screenshot: could not write the picture");
                e.printStackTrace();
                return null;
            }
            return uri.toString();
        }

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            dir = new File(context.getFilesDir(), "pictures");
        }
        if (!dir.isDirectory() && !dir.mkdirs()) {
            return null;
        }
        File file = new File(dir, name);
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                return null;
            }
        } catch (Exception e) {
            System.err.println("Screenshot: could not write the picture");
            e.printStackTrace();
            return null;
        }
        return file.getAbsolutePath();
    }
}
