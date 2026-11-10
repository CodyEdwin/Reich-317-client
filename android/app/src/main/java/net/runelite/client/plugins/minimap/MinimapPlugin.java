package net.runelite.client.plugins.minimap;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MouseWheelMoved;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Android variant of RuneLite's Minimap plugin.
 *
 * <p>Same features as the desktop plugin: recolour the minimap dots, hide the
 * minimap and zoom it. Two adaptations:
 * <ul>
 *   <li>Colours are ARGB ints (no {@code java.awt.Color}); {@code 0} keeps the
 *       vanilla dot.</li>
 *   <li>Zooming on the minimap listens for the Android {@link MouseWheelMoved}
 *       event instead of RuneLite's AWT {@code MouseManager}.</li>
 * </ul>
 */
@PluginDescriptor(
        name = "Minimap",
        description = "Customize the color of minimap dots, hide the minimap, and zoom",
        tags = {"items", "npcs", "players", "zoom"}
)
public class MinimapPlugin extends Plugin {

    private static final int DOT_ITEM = 0;
    private static final int DOT_NPC = 1;
    private static final int DOT_PLAYER = 2;
    private static final int DOT_FRIEND = 3;
    private static final int DOT_TEAM = 4;
    private static final int DOT_FRIENDSCHAT = 5;
    private static final int DOT_CLAN = 6;

    @Inject
    private Client client;

    @Inject
    private MinimapConfig config;

    private SpritePixels[] originalDotSprites;

    @Override
    protected void startUp() {
        updateMinimapWidgetVisibility(config.hideMinimap());
        storeOriginalDots();
        replaceMapDots();
        client.setMinimapZoom(config.zoom());

        if (config.hdMinimap()) {
            client.setHdMinimapEnabled(true);
            client.setGameState(1);
        }
    }

    @Override
    protected void shutDown() {
        updateMinimapWidgetVisibility(false);
        restoreOriginalDots();
        client.setMinimapZoom(false);
        client.setHdMinimapEnabled(false);
        client.setGameState(1);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN && originalDotSprites == null) {
            storeOriginalDots();
            replaceMapDots();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!MinimapConfig.GROUP.equals(event.getGroup())) {
            return;
        }

        if ("hideMinimap".equals(event.getKey())) {
            updateMinimapWidgetVisibility(config.hideMinimap());
            return;
        } else if ("zoom".equals(event.getKey())) {
            client.setMinimapZoom(config.zoom());
            return;
        } else if ("hdminimap".equals(event.getKey())) {
            client.setHdMinimapEnabled(config.hdMinimap());
            client.setGameState(1);
            return;
        }
        replaceMapDots();
    }

    /** Zooms the minimap when the wheel is turned while the pointer is on it. */
    @Subscribe
    public void onMouseWheelMoved(MouseWheelMoved event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Point position = client.getMouseCanvasPosition();
        if (position == null) {
            return;
        }

        int width = (int) client.getStretchedDimensions().getWidth();
        if (!inMap(position.getX(), position.getY(), client.isResized(), width) || !client.isMinimapZoom()) {
            return;
        }

        int zoom = (int) client.getMinimapZoom();
        zoom += event.getRotation() * 35;
        if (zoom > 210) {
            zoom = 210;
        }
        if (zoom < -70) {
            zoom = -70;
        }
        client.setMinimapZoom(zoom);
    }

    private void updateMinimapWidgetVisibility(boolean enable) {
        Widget resizableStonesWidget = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_WIDGET);
        if (resizableStonesWidget != null) {
            resizableStonesWidget.setHidden(enable);
        }

        Widget resizableNormalWidget = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_WIDGET);
        if (resizableNormalWidget != null && !resizableNormalWidget.isSelfHidden()) {
            for (Widget widget : resizableNormalWidget.getStaticChildren()) {
                if (widget.getId() != WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_LOGOUT_BUTTON.getId()
                        && widget.getId() != WidgetInfo.RESIZABLE_MINIMAP_LOGOUT_BUTTON.getId()) {
                    widget.setHidden(enable);
                }
            }
        }
    }

    private void replaceMapDots() {
        SpritePixels[] mapDots = client.getMapDots();
        if (mapDots == null) {
            return;
        }

        applyDot(mapDots, DOT_ITEM, config.itemColor());
        applyDot(mapDots, DOT_NPC, config.npcColor());
        applyDot(mapDots, DOT_PLAYER, config.playerColor());
        applyDot(mapDots, DOT_FRIEND, config.friendColor());
        applyDot(mapDots, DOT_TEAM, config.teamColor());
        applyDot(mapDots, DOT_FRIENDSCHAT, config.friendsChatColor());
        applyDot(mapDots, DOT_CLAN, config.clanChatColor());
    }

    private void applyDot(SpritePixels[] mapDots, int id, int color) {
        // 0 means "not configured": keep the vanilla dot.
        if (id < mapDots.length && color != 0) {
            mapDots[id] = MinimapDot.create(client, color);
        }
    }

    private void storeOriginalDots() {
        SpritePixels[] originalDots = client.getMapDots();
        if (originalDots == null) {
            return;
        }
        originalDotSprites = Arrays.copyOf(originalDots, originalDots.length);
    }

    private void restoreOriginalDots() {
        SpritePixels[] mapDots = client.getMapDots();
        if (originalDotSprites == null || mapDots == null) {
            return;
        }
        System.arraycopy(originalDotSprites, 0, mapDots, 0, mapDots.length);
    }

    private boolean inMap(int mouseX, int mouseY, boolean resized, int width) {
        int i = mouseX - 25 - 547;
        int j = mouseY - 5 - 3;
        if (resized) {
            i = mouseX - (width - 182 + 24);
            j = mouseY - 8;
        }
        return inCircle(0, 0, i, j, 76);
    }

    private boolean inCircle(int circleX, int circleY, int clickX, int clickY, int radius) {
        return Math.pow(circleX + radius - clickX, 2)
                + Math.pow(circleY + radius - clickY, 2) < Math.pow(radius, 2);
    }
}
