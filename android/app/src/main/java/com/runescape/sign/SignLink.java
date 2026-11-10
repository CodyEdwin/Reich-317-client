package com.runescape.sign;

import com.runescape.Configuration;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Android replacement for the desktop {@code SignLink}.
 *
 * <p>The desktop version opens {@code ./Cache/main_file_cache.dat} (and the
 * {@code .idx*} files) and grabs the AWT system {@code EventQueue}. Android has
 * no AWT and the cache lives in the app's internal storage, so this version
 * opens the cache from {@link #cacheDir} (set by {@link ClientService} once the
 * asset cache has been unpacked) and drops the AWT bits. The (package-private)
 * API otherwise matches the desktop class so {@code Client.java} is unchanged.
 */
public final class SignLink {

    public static final RandomAccessFile[] indices = new RandomAccessFile[6];
    public static RandomAccessFile cache_dat = null;
    public static Object mainapp = null;
    public static String os;
    public static String arch;

    /** Absolute path (with a trailing separator) to the unpacked cache directory. */
    public static String cacheDir = null;

    private SignLink() {
    }

    public static void init(Object px) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        mainapp = px;

        String directory = findcachedir();
        try {
            cache_dat = new RandomAccessFile(directory + "main_file_cache.dat", "rw");
            for (int index = 0; index < 6; index++) {
                indices[index] = new RandomAccessFile(directory + "main_file_cache.idx" + index, "rw");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        os = null;
        try {
            os = System.getProperty("os.name").toLowerCase();
        } catch (Throwable ignored) {
        }
        arch = null;
        try {
            arch = System.getProperty("os.arch").toLowerCase();
        } catch (Throwable ignored) {
        }
    }

    public static String trim(String s) {
        if (s == null || s.length() == 0)
            return null;

        s = s.trim();
        return s.length() == 0 ? null : s;
    }

    public static String findcachedir() {
        if (cacheDir != null) {
            return cacheDir;
        }
        final File cacheDirectory = new File(Configuration.CACHE_DIRECTORY);
        if (!cacheDirectory.exists())
            cacheDirectory.mkdir();
        return Configuration.CACHE_DIRECTORY;
    }

    public static String indexLocation(int cacheIndex, int index) {
        return findcachedir() + "index" + cacheIndex + "/"
                + (index != -1 ? index + ".gz" : "");
    }
}
