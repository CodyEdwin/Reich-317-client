package com.elvarg.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.runescape.sign.SignLink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Hosts the game client in a foreground service, so leaving the app does not
 * reset it. The client renders into {@link AndroidDisplay} and takes all of its
 * input through {@link ClientEngine}, so it has no dependency on an activity or
 * view; {@link MainActivity} can come and go and simply re-attaches to the
 * engine that is already running.
 */
public final class ClientService extends Service {

    public static final String ACTION_QUIT = "com.elvarg.client.action.QUIT";

    private static final String CHANNEL_ID = "elvarg-client";
    private static final String MESSAGE_CHANNEL_ID = "elvarg-messages";
    private static final int NOTIFICATION_ID = 1;
    private static final int MESSAGE_NOTIFICATION_ID = 2;

    private static volatile ClientService current;
    private static volatile ClientEngine engine;
    private static volatile String status = "Preparing cache\u2026";

    private volatile boolean starting;
    private volatile boolean destroyed;

    public static ClientEngine engine() {
        return engine;
    }

    public static boolean isRunning() {
        return current != null;
    }

    /** The running service, or null when the client is not started. */
    public static ClientService currentInstance() {
        return current;
    }

    public static String statusText() {
        ClientEngine running = engine;
        return running != null ? running.status : status;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        current = this;
        createNotificationChannel();
        // Plugin notifications become system notifications.
        com.elvarg.android.rl.AndroidPluginHost.setNotificationSink(this::showNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_QUIT.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification(status));

        if (engine != null || starting) {
            return START_NOT_STICKY;
        }
        starting = true;

        Thread thread = new Thread(this::boot, "elvarg-boot");
        thread.setDaemon(true);
        thread.start();
        return START_NOT_STICKY;
    }

    private void boot() {
        try {
            prepareCache();
            if (destroyed) {
                return;
            }
            setStatus("Booting client\u2026");
            ClientEngine created = new ClientEngine();
            engine = created;
            created.start();
            updateNotification(buildNotification(getString(R.string.notif_client_running)));
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Failed: " + e);
            updateNotification(buildNotification(status));
        } finally {
            starting = false;
        }
    }

    @Override
    public void onDestroy() {
        current = null;
        destroyed = true;
        ClientEngine running = engine;
        engine = null;
        if (running != null) {
            running.shutdown();
        }
        status = "Preparing cache\u2026";
        super.onDestroy();
    }

    /**
     * Copies the cache out of the APK on first run. The client reads it with
     * {@link java.io.RandomAccessFile}, so it has to be a real file tree under
     * internal storage. Existing files are kept: the client writes files it
     * downloads from the server back into this cache.
     */
    private void prepareCache() throws Exception {
        File dir = new File(getFilesDir(), "Cache");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IllegalStateException("could not create " + dir);
        }
        copyAssetTree("", dir);
        SignLink.cacheDir = dir.getAbsolutePath() + "/";
    }

    private void copyAssetTree(String assetPath, File targetDir) throws IOException {
        String[] children = assetPath.isEmpty()
                ? getAssets().list("")
                : getAssets().list(assetPath);
        if (children == null) {
            return;
        }
        for (String child : children) {
            String childPath = assetPath.isEmpty() ? child : assetPath + "/" + child;
            String[] grandChildren = getAssets().list(childPath);
            if (grandChildren != null && grandChildren.length > 0) {
                File sub = new File(targetDir, child);
                if (!sub.isDirectory() && !sub.mkdirs()) {
                    throw new IOException("could not create " + sub);
                }
                copyAssetTree(childPath, sub);
            } else {
                copyAssetFile(childPath, new File(targetDir, child));
            }
        }
    }

    private void copyAssetFile(String assetPath, File outFile) throws IOException {
        if (outFile.isFile() && outFile.length() > 0L) {
            return;
        }
        try (InputStream in = getAssets().open(assetPath);
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
        }
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent quit = new Intent(this, ClientService.class).setAction(ACTION_QUIT);
        PendingIntent quitIntent = PendingIntent.getService(this, 1, quit,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_client_title))
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .addAction(0, getString(R.string.action_quit), quitIntent)
                .build();
    }

    private void updateNotification(Notification notification) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Shows a plugin notification in the shade.
     *
     * <p>Android has no system tray, so the notifications the desktop client
     * shows as balloons become ordinary system notifications here.
     */
    public void showNotification(String message) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        Intent open = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 2, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this, MESSAGE_CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_client_title))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(MESSAGE_NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notif_channel_description));
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);

        NotificationChannel messages = new NotificationChannel(
                MESSAGE_CHANNEL_ID,
                getString(R.string.notif_message_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        messages.setDescription(getString(R.string.notif_channel_description));
        manager.createNotificationChannel(messages);
    }

    private void setStatus(String s) {
        status = s;
        System.out.println("[ElvargClient] " + s);
    }
}
