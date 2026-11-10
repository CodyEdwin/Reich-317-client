package com.elvarg.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.elvarg.android.rl.AndroidPluginHost;
import com.runescape.Client;
import com.runescape.loginscreen.worlds.WorldData;
import com.runescape.loginscreen.worlds.WorldManager;

import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;

import java.util.List;

/**
 * The Android replacement for the RuneLite sidebar.
 *
 * <p>RuneLite's desktop UI is a Swing application with a plugin list, a
 * configuration panel and a set of built-in panels. On Android those are
 * replaced by this dialog-based in-game menu, reachable from the <em>Menu</em>
 * button. It operates on the same {@link PluginManager}, {@link ConfigManager}
 * and world list the rest of the port uses.
 *
 * <p>Every action that changes client/plugin state is queued with
 * {@link ClientEngine#post(Runnable)} so it runs on the game thread, where the
 * plugin runtime lives.
 */
public final class GameMenu {

    private final MainActivity activity;

    private GameMenu(MainActivity activity) {
        this.activity = activity;
    }

    public static void show(MainActivity activity, View anchor) {
        new GameMenu(activity).showMainMenu(anchor);
    }

    private void showMainMenu(View anchor) {
        Runnable keyboard = activity::toggleKeyboard;
        Runnable rightClick = activity::toggleRightClick;
        Runnable plugins = this::showPluginList;
        Runnable settings = this::showSettings;
        Runnable worlds = this::showWorldSwitcher;
        Runnable notifications = this::showNotifications;
        Runnable screenshot = activity::takeScreenshot;
        Runnable logout = this::confirmLogout;
        Runnable quit = activity::quit;

        final String[] items = {
                activity.getString(R.string.menu_plugins),
                activity.getString(R.string.menu_settings),
                activity.getString(R.string.menu_worlds),
                activity.getString(R.string.menu_notifications),
                activity.getString(R.string.menu_screenshot),
                activity.getString(R.string.menu_logout),
                activity.getString(R.string.menu_keyboard),
                rightClickLabel(),
                activity.getString(R.string.action_quit),
        };
        final Runnable[] actions = {plugins, settings, worlds, notifications, screenshot, logout, keyboard, rightClick, quit};

        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_title)
                .setItems(items, (dialog, which) -> actions[which].run())
                .setNegativeButton(R.string.menu_close, null)
                .show();
    }

    private String rightClickLabel() {
        return activity.getString(R.string.menu_right_click)
                + (activity.isRightClickNext() ? ": on" : "");
    }

    // ---- Plugins ----------------------------------------------------------

    private void showPluginList() {
        AndroidPluginHost host = host();
        if (host == null) {
            return;
        }
        PluginManager pluginManager = host.getPluginManager();
        List<Plugin> plugins = pluginManager.getPlugins();
        if (plugins.isEmpty()) {
            toast(R.string.menu_no_plugins);
            return;
        }

        String[] names = new String[plugins.size()];
        boolean[] checked = new boolean[plugins.size()];
        for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = plugins.get(i);
            names[i] = plugin.getName();
            checked[i] = pluginManager.isPluginEnabled(plugin);
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_plugins)
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> {
                    Plugin plugin = plugins.get(which);
                    ClientEngine.post(() -> pluginManager.setPluginEnabled(plugin, isChecked));
                })
                .setPositiveButton(R.string.menu_close, null)
                .show();
    }

    // ---- Settings ---------------------------------------------------------

    private void showSettings() {
        AndroidPluginHost host = host();
        if (host == null) {
            return;
        }
        ConfigEditor.show(activity, host);
    }

    // ---- World switcher ---------------------------------------------------

    private void showWorldSwitcher() {
        // LoginScreen.setUp() loads this at startup (with a built-in fallback),
        // so do not touch the network from the activity thread.
        final List<WorldData> worlds = WorldManager.INSTANCE.getWorldList();
        if (worlds.isEmpty()) {
            toast(R.string.menu_no_worlds);
            return;
        }

        String[] names = new String[worlds.size()];
        for (int i = 0; i < worlds.size(); i++) {
            WorldData world = worlds.get(i);
            names[i] = world.getName() + " (" + world.getType().name() + ")";
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_worlds)
                .setItems(names, (dialog, which) -> confirmWorldSwitch(worlds.get(which)))
                .setNegativeButton(R.string.menu_close, null)
                .show();
    }

    private void confirmWorldSwitch(WorldData world) {
        String message = activity.getString(R.string.menu_world_switch_message, world.getName());
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_worlds)
                .setMessage(message)
                .setPositiveButton(R.string.menu_world_switch, (dialog, which) -> ClientEngine.post(() -> switchWorld(world)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void switchWorld(WorldData world) {
        Client client = Client.instance;
        if (client == null) {
            return;
        }
        if (Client.loggedIn) {
            client.logout();
        }
        WorldManager.INSTANCE.setSelectedWorld(world);
        Client.server = world.getIp();
    }

    // ---- Notifications ----------------------------------------------------

    /** The notifications the plugins raised, newest first. */
    private void showNotifications() {
        AndroidPluginHost host = host();
        if (host == null) {
            return;
        }
        Notifier notifier = host.getNotifier();
        List<Notifier.Notification> notifications = notifier.getNotifications();
        if (notifications.isEmpty()) {
            toast(R.string.menu_no_notifications);
            return;
        }

        String[] messages = new String[notifications.size()];
        for (int i = 0; i < notifications.size(); i++) {
            messages[i] = notifications.get(i).getMessage();
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_notifications)
                .setItems(messages, (dialog, which) -> toast(messages[which]))
                .setNeutralButton(R.string.menu_clear_notifications, (dialog, which) -> notifier.clear())
                .setPositiveButton(R.string.menu_close, null)
                .show();
    }

    // ---- Logout / quit ----------------------------------------------------

    private void confirmLogout() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_logout)
                .setMessage(R.string.menu_logout_message)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> ClientEngine.post(() -> {
                    Client client = Client.instance;
                    if (client != null) {
                        client.logout();
                    }
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ---- Helpers ----------------------------------------------------------

    /**
     * Returns the plugin host, or reports that the client is still starting.
     * Deliberately uses {@link AndroidPluginHost#getInstance()} rather than
     * {@code ensureStarted()}: the host must be created on the game thread, not
     * from the activity.
     */
    private AndroidPluginHost host() {
        AndroidPluginHost host = AndroidPluginHost.getInstance();
        if (host == null) {
            toast(R.string.menu_client_starting);
        }
        return host;
    }

    private void toast(int stringId) {
        Toast.makeText(activity, stringId, Toast.LENGTH_SHORT).show();
    }

    private void toast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
