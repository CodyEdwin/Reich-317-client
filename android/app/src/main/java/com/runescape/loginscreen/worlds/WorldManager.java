package com.runescape.loginscreen.worlds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runescape.Client;
import com.runescape.Configuration;
import com.runescape.cache.graphics.ImageCache;
import com.runescape.draw.Rasterizer2D;
import com.runescape.engine.GameEngine;
import com.runescape.engine.impl.MouseHandler;
import com.runescape.loginscreen.LoginState;
import com.runescape.sign.SignLink;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Android port of the desktop world list.
 *
 * <p>The desktop client reads {@code ./worldData.json} relative to the working
 * directory. An Android app has no useful working directory (it is {@code /}),
 * so the file ships as an asset: {@code ClientService} unpacks the assets into
 * the cache directory, and it is read from {@link SignLink#findcachedir()}
 * instead. A missing or malformed file must never take the client down, so it
 * falls back to a built-in world list.
 *
 * <p>Java port of the desktop Kotlin {@code object}; the {@code INSTANCE}
 * field keeps the call sites identical.
 */
public final class WorldManager {

    private static final boolean LOAD_WORLDS_ONLINE = false;

    private static final String WORLD_DATA_URL = "";

    private static final String WORLD_DATA_FILE = "worldData.json";

    private static final Gson GSON = new Gson();

    /** Mirrors the singleton accessor of the Kotlin {@code object}. */
    public static final WorldManager INSTANCE = new WorldManager();

    private boolean loadedWorlds;

    private List<WorldData> worldList = new ArrayList<>();

    private String worldStatusText = "Click to switch";

    private WorldData selectedWorld;

    private WorldManager() {
    }

    public boolean isLoadedWorlds() {
        return loadedWorlds;
    }

    public void setLoadedWorlds(boolean loadedWorlds) {
        this.loadedWorlds = loadedWorlds;
    }

    public List<WorldData> getWorldList() {
        return worldList;
    }

    public void setWorldList(List<WorldData> worldList) {
        this.worldList = worldList;
    }

    public String getWorldStatusText() {
        return worldStatusText;
    }

    public void setWorldStatusText(String worldStatusText) {
        this.worldStatusText = worldStatusText;
    }

    public WorldData getSelectedWorld() {
        return selectedWorld;
    }

    public void setSelectedWorld(WorldData selectedWorld) {
        this.selectedWorld = selectedWorld;
    }

    public void openWorldSectionScreen(boolean switchView) {
        worldStatusText = "Loading...";

        String worldContent = null;
        if (LOAD_WORLDS_ONLINE) {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(WORLD_DATA_URL)
                        .build();
                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (response.body() != null) {
                        worldContent = response.body().string();
                    } else {
                        worldStatusText = "Error";
                        loadedWorlds = false;
                    }
                }
            } catch (IOException | RuntimeException e) {
                worldStatusText = "Error";
                loadedWorlds = false;
            }
        } else {
            worldContent = readLocalWorldData();
        }

        worldList = parseWorldData(worldContent);
        if (worldList.isEmpty()) {
            worldList = defaultWorlds();
        }
        worldStatusText = "Click to switch";
        loadedWorlds = true;
        if (switchView && Client.loginScreen != null) {
            Client.loginScreen.setLoginState(LoginState.WORLD_SELECT);
        }
    }

    /** Reads the bundled world list from the cache directory (see class docs). */
    private String readLocalWorldData() {
        File[] candidates = {
                new File(SignLink.findcachedir(), WORLD_DATA_FILE),
                new File(WORLD_DATA_FILE),
        };
        for (File file : candidates) {
            try {
                if (file.isFile()) {
                    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                // Try the next location.
            }
        }
        System.out.println("WorldManager: " + WORLD_DATA_FILE + " not found, using built-in world list");
        return null;
    }

    private List<WorldData> parseWorldData(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Type type = new TypeToken<ArrayList<WorldData>>() {
            }.getType();
            List<WorldData> parsed = GSON.fromJson(content, type);
            return parsed == null ? Collections.<WorldData>emptyList() : parsed;
        } catch (RuntimeException e) {
            System.out.println("WorldManager: could not parse " + WORLD_DATA_FILE + ": " + e);
            return Collections.emptyList();
        }
    }

    private List<WorldData> defaultWorlds() {
        List<WorldData> worlds = new ArrayList<>();
        worlds.add(new WorldData(Configuration.SERVER_ADDRESS, "Local", WorldType.NORMAL,
                "Local Development World"));
        return worlds;
    }

    /**
     * Click handling for the world list.
     *
     * <p>This runs from the client tick (see {@code LoginScreen.handleInput})
     * rather than from {@link #renderWorldSelect()}: clicks are only visible in
     * {@code MouseHandler.clickMode3} until the next client tick, and a slow
     * frame (routine on Android) runs several catch-up ticks before it draws,
     * so draw-time click checks drop most presses.
     */
    public void handleInput() {
        if (Client.instance.newclickInRegion(GameEngine.canvasWidth - 57, 4, 50, 16)) {
            Client.loginScreen.setLoginState(LoginState.LOGIN);
        }

        final int worldListX = 56;
        final int worldListY = 35;

        int xOffset = 0;
        int yOffset = 0;

        for (int index = 0; index < worldList.size(); index++) {
            WorldData world = worldList.get(index);

            int worldButtonX = worldListX + xOffset;
            int worldButtonY = worldListY + yOffset;

            if (Client.instance.newclickInRegion(worldButtonX, worldButtonY, ImageCache.get(31))) {
                Client.server = world.getIp();
                Client.loginScreen.setLoginState(LoginState.LOGIN);
            }

            if (index % 7 == 0) {
                yOffset += 19;
                xOffset = 0;
            } else {
                xOffset += 94;
                yOffset = 0;
            }
        }
    }

    public void renderWorldSelect() {
        Rasterizer2D.drawBox(0, 0, GameEngine.canvasWidth, GameEngine.canvasHeight, 0x000000);
        ImageCache.get(27).drawAdvancedSprite(0, 0);

        final int worldListX = 56;
        final int worldListY = 35;

        int xOffset = 0;
        int yOffset = 0;

        for (int index = 0; index < worldList.size(); index++) {
            WorldData world = worldList.get(index);

            int worldButtonX = worldListX + xOffset;
            int worldButtonY = worldListY + yOffset;

            ImageCache.get(31).drawSprite(worldButtonX, worldButtonY);

            if (Client.instance.mouseInRegion(worldButtonX, worldButtonY, ImageCache.get(31))) {
                drawTooltip(world);
            }

            Client.instance.newBoldFont.drawBasicString(world.getName(), worldButtonX + 1, worldButtonY + 14, 0x111111);

            Client.instance.newSmallFont.drawCenteredString(
                    "?",
                    worldButtonX + 37 + (37 / 2), worldButtonY + 15,
                    0xFFFFFF, 0);

            if (index % 7 == 0) {
                yOffset += 19;
                xOffset = 0;
            } else {
                xOffset += 94;
                yOffset = 0;
            }
        }
    }

    public void drawTooltip(WorldData world) {
        String[] lines = world.getDescription().split("<br>");
        int height = (10 * lines.length) + 4;
        int width = 2;
        for (String line : lines) {
            int textWidth = Client.instance.smallText.getTextWidth(line);
            if (textWidth >= width) {
                width = textWidth;
            }
        }

        width += 6;

        int x = MouseHandler.mouseX - (width / 2);
        int y = MouseHandler.mouseY + 20;

        Rasterizer2D.drawBoxOutline(x, y, width + 2, height + 2, 0x000000);
        Rasterizer2D.drawBox(x + 1, y + 1, width, height, 0xFFFFA0);
        for (int index = 0; index < lines.length; index++) {
            Client.instance.newSmallFont.drawCenteredString(lines[index], x + (width / 2), y + 12 + (10 * index), 0x111111);
        }
    }
}
