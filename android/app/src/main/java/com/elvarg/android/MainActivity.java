package com.elvarg.android;

import com.elvarg.android.rl.AndroidPluginHost;

import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows the game client hosted by {@link ClientService}.
 *
 * <p>This activity only draws the framebuffer and forwards input: the client
 * itself lives in the service, so leaving the app does not restart it. The
 * on-screen bar provides the touch affordances the desktop client got from the
 * mouse and keyboard, and the <em>Menu</em> button is the entry point for the
 * Android replacement of the RuneLite sidebar (plugins, settings, world
 * switcher, logout).
 */
public final class MainActivity extends Activity {

    GameView gameView;
    private TextView statusView;
    private Button rightButton;
    private LinearLayout bar;
    private volatile boolean destroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        gameView = new GameView(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        statusView = new TextView(this);
        statusView.setTextColor(Color.WHITE);
        statusView.setShadowLayer(4f, 0f, 1f, Color.BLACK);
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        statusView.setText(ClientService.statusText());
        statusView.setPadding(dp(10), dp(6), dp(10), dp(6));
        FrameLayout.LayoutParams statusLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        statusLp.gravity = Gravity.TOP | Gravity.START;
        root.addView(statusView, statusLp);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(0x88121212);
        bar.setPadding(dp(6), dp(6), dp(6), dp(6));
        this.bar = bar;

        Button menuButton = tapButton(getString(R.string.menu_open), this::openMenu);
        Button kbButton = tapButton("\u2328 " + getString(R.string.menu_keyboard), v -> toggleKeyboard());
        rightButton = tapButton(getString(R.string.menu_right_click), v -> toggleRightClick());
        Button quitButton = tapButton(getString(R.string.menu_quit), v -> quit());
        bar.addView(menuButton);
        bar.addView(kbButton);
        bar.addView(rightButton);
        bar.addView(quitButton);

        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        barLp.gravity = Gravity.BOTTOM | Gravity.END;
        root.addView(bar, barLp);

        setContentView(root);
        gameView.requestFocus();
        gameView.setOverlayMenuListener(this::showOverlayMenu);

        refreshButtons();
        startClient();
        statusView.postDelayed(this::refreshStatus, 300);
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachEngine();
        notifyFocus(true);
    }

    @Override
    protected void onPause() {
        notifyFocus(false);
        super.onPause();
    }

    /** Lets the client (and the FPS limiter) know whether the app is visible. */
    private void notifyFocus(boolean focused) {
        ClientEngine engine = ClientService.engine();
        if (engine != null) {
            engine.focusChanged(focused);
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        gameView.setEngine(null);
        super.onDestroy();
    }

    private void startClient() {
        requestNotificationPermission();
        startForegroundService(new Intent(this, ClientService.class));
    }

    void quit() {
        stopService(new Intent(this, ClientService.class));
        finish();
    }

    private void attachEngine() {
        ClientEngine engine = ClientService.engine();
        if (engine != null) {
            gameView.setEngine(engine);
        }
    }

    /**
     * The Android replacement for the RuneLite sidebar: plugin list, config
     * editor, world switcher, logout and the input toggles. See
     * {@link GameMenu}.
     */
    private void openMenu(View anchor) {
        GameMenu.show(this, anchor);
    }

    /** Takes a picture of the current frame and saves it to the device. */
    void takeScreenshot() {
        android.graphics.Bitmap shot = gameView.snapshot();
        if (shot == null) {
            toast(getString(R.string.menu_screenshot_failed));
            return;
        }
        String location = Screenshot.save(this, shot);
        toast(location == null
                ? getString(R.string.menu_screenshot_failed)
                : getString(R.string.menu_screenshot_saved, location));
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows the right-click menu of the overlay under the cursor.
     *
     * <p>On the desktop the overlay menus are injected into the game's own
     * right-click menu; on Android they are shown as a dialog, and picking an
     * entry posts {@link OverlayMenuClicked} so the plugin can react to it.
     */
    private void showOverlayMenu(int canvasX, int canvasY) {
        AndroidPluginHost host = AndroidPluginHost.getInstance();
        if (host == null) {
            return;
        }
        Overlay overlay = host.getOverlayManager().getOverlayAt(canvasX, canvasY);
        if (overlay == null) {
            return;
        }
        List<OverlayMenuEntry> entries = overlay.getMenuEntries();
        if (entries.isEmpty()) {
            return;
        }

        String[] options = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            options[i] = entries.get(i).getOption();
        }

        new AlertDialog.Builder(this)
                .setTitle(overlay.getName())
                .setItems(options, (dialog, which) -> host.getEventBus()
                        .post(new OverlayMenuClicked(entries.get(which), overlay)))
                .setNegativeButton(R.string.menu_close, null)
                .show();
    }

    boolean isRightClickNext() {
        return gameView.rightClickNext;
    }    void toggleRightClick() {
        gameView.rightClickNext = !gameView.rightClickNext;
        refreshButtons();
    }

    private Button tapButton(String label, View.OnClickListener click) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        button.setBackgroundColor(0xFF2A2A2A);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(38));
        lp.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(lp);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setOnClickListener(click);
        return button;
    }

    void toggleKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        gameView.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void refreshButtons() {
        if (rightButton != null) {
            boolean right = gameView.rightClickNext;
            rightButton.setText(right ? getString(R.string.menu_right_click) + ": on" : getString(R.string.menu_right_click));
            rightButton.setBackgroundColor(right ? 0xFF8D1D1D : 0xFF2A2A2A);
        }
    }

    private void refreshStatus() {
        if (destroyed) {
            return;
        }
        attachEngine();
        applyClientSettings();
        statusView.setText(ClientService.statusText());
        statusView.postDelayed(this::refreshStatus, 400);
    }

    /** Applies the client-wide settings (the on-screen button bar). */
    private void applyClientSettings() {
        if (bar == null) {
            return;
        }
        AndroidPluginHost host = AndroidPluginHost.getInstance();
        if (host == null) {
            return;
        }
        boolean show = host.getConfigManager()
                .getConfig(net.runelite.client.config.RuneLiteConfig.class)
                .showOnScreenBar();
        bar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        ClientEngine engine = ClientService.engine();
        int keyCode = event.getKeyCode();
        if (engine != null
                && keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                engine.keyDown(KeyMap.toAwtKeyCode(keyCode), (char) event.getUnicodeChar(event.getMetaState()));
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                engine.keyUp(KeyMap.toAwtKeyCode(keyCode), (char) event.getUnicodeChar(event.getMetaState()));
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
