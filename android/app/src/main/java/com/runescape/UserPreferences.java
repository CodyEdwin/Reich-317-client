package com.runescape;

import com.google.gson.Gson;
import com.runescape.sign.SignLink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Loads and stores {@link PreferencesData} as {@code settings.json}.
 *
 * <p>Java port of the desktop Kotlin {@code object}; the {@code INSTANCE}
 * field keeps the call sites identical.
 *
 * <p>The desktop client serialises the settings with Klaxon. Klaxon's API is
 * inline/reified, which Java cannot call, so the Android port writes the same
 * JSON with Gson instead (the file format is identical: the property names are
 * the JSON keys).
 */
public final class UserPreferences {

    private static final Gson GSON = new Gson();

    /** Mirrors the singleton accessor of the Kotlin {@code object}. */
    public static final UserPreferences INSTANCE = new UserPreferences();

    private UserPreferences() {
    }

    public void load(Client client) {
        File file = new File(SignLink.findcachedir(), "settings.json");

        try {
            if (!file.exists()) {
                file.createNewFile();
                save();
            }
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            PreferencesData parsed = GSON.fromJson(content, PreferencesData.class);
            if (parsed != null) {
                Client.preferences = parsed;
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        File file = new File(SignLink.findcachedir(), "settings.json");
        try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
            output.write(GSON.toJson(Client.instance.preferences));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
