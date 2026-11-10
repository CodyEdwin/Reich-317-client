package com.elvarg.android;

import java.util.List;

import com.elvarg.android.gfx.Graphics2D;
import com.elvarg.android.input.KeyStroke;
import com.elvarg.android.input.PointerEvent;
import com.elvarg.android.rl.AndroidPluginHost;

import net.runelite.api.MainBufferProvider;
import net.runelite.api.hooks.Callbacks;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;

/**
 * Android implementation of {@link Callbacks}, the hook surface the client calls
 * into for RuneLite.
 *
 * <p>On the desktop these callbacks are supplied by the RuneLite host
 * ({@code net.runelite.client.callback.Hooks}), which fans events out to the
 * plugin manager and draws overlays onto an AWT {@code Graphics}. On Android the
 * client owns its own {@code Callbacks}; this class is the single place the
 * event bus and overlay renderer plug in.
 *
 * <p>In the boot phase (no plugins loaded) it is intentionally inert: posted
 * events are dropped, pointer/keyboard events pass through unconsumed so the
 * client's own input handling still runs, and the overlay draw hook does nothing.
 * The plugin/overlay phases extend this class rather than replacing it.
 */
public final class AndroidCallbacks implements Callbacks {

    public static final AndroidCallbacks INSTANCE = new AndroidCallbacks();

    private AndroidCallbacks() {
    }

    @Override
    public void post(Object event) {
        AndroidPluginHost.post(event);
    }

    @Override
    public void postDeferred(Object event) {
        AndroidPluginHost.post(event);
    }

    @Override
    public void tick() {
        ClientEngine.drainTasks();
        AndroidPluginHost.gameTick();
    }

    @Override
    public void frame() {
        ClientEngine.drainTasks();
        AndroidPluginHost.beforeRender();
    }

    @Override
    public void updateNpcs() {
    }

    @Override
    public void drawScene() {
    }

    @Override
    public void drawAboveOverheads() {
    }

    @Override
    public void draw(MainBufferProvider mainBufferProvider, Graphics2D graphics, int x, int y) {
        // The raster provider has already blitted the raw frame to AndroidDisplay;
        // overlays are painted on top of it here.
        AndroidPluginHost.drawOverlays(graphics);
    }

    @Override
    public void drawInterface(int interfaceId, List<WidgetItem> widgetItems) {
    }

    @Override
    public void drawLayer(Widget layer, List<WidgetItem> widgetItems) {
    }

    @Override
    public PointerEvent mousePressed(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseReleased(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseClicked(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseEntered(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseExited(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseDragged(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseMoved(PointerEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public PointerEvent mouseWheelMoved(PointerEvent event) {
        return event;
    }

    @Override
    public void keyPressed(KeyStroke keyEvent) {
    }

    @Override
    public void keyReleased(KeyStroke keyEvent) {
    }

    @Override
    public void keyTyped(KeyStroke keyEvent) {
    }
}
