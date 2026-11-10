package net.runelite.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.runescape.sign.SignLink;

import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Android implementation of RuneLite's configuration store.
 *
 * <p>Values live in memory as {@code group -> key -> string} and are persisted
 * as JSON in the client's data directory ({@code runelite-config.json}), next to
 * the cache. {@link ConfigChanged} is posted on every real change so plugins can
 * react exactly as on desktop.
 */
public final class ConfigManager {

    private static final String DEFAULT_FILE = "runelite-config.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, Map<String, String>> groups = new LinkedHashMap<>();
    private final Map<Class<?>, Object> configs = new HashMap<>();

    private volatile EventBus eventBus;

    public ConfigManager() {
        this(new File(SignLink.findcachedir(), DEFAULT_FILE));
    }

    public ConfigManager(File file) {
        this.file = file;
        load();
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Returns the singleton configuration instance for {@code type}. The class
     * must extend {@link ConfigBase} and have a no-argument constructor.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getConfig(Class<T> type) {
        Object existing = configs.get(type);
        if (existing != null) {
            return (T) existing;
        }
        if (!ConfigBase.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName()
                    + " must extend " + ConfigBase.class.getName()
                    + "; Android configs cannot be interfaces with default methods");
        }
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            ((ConfigBase) instance).attach(this);
            configs.put(type, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate config " + type.getName(), e);
        }
    }

    public synchronized String getConfiguration(String group, String key) {
        Map<String, String> values = groups.get(group);
        return values == null ? null : values.get(key);
    }

    public synchronized String getConfiguration(String group, String key, String defaultValue) {
        String value = getConfiguration(group, key);
        return value != null ? value : defaultValue;
    }

    public synchronized boolean getBoolean(String group, String key, boolean defaultValue) {
        String value = getConfiguration(group, key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public synchronized int getInt(String group, String key, int defaultValue) {
        String value = getConfiguration(group, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public synchronized double getDouble(String group, String key, double defaultValue) {
        String value = getConfiguration(group, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Stores a value and posts {@link ConfigChanged}. Passing {@code null}
     * removes the entry (the accessor's default applies again).
     */
    public void setConfiguration(String group, String key, String value) {
        String oldValue;
        synchronized (this) {
            Map<String, String> values = groups.computeIfAbsent(group, g -> new TreeMap<>());
            oldValue = values.get(key);
            if (value == null ? oldValue == null : value.equals(oldValue)) {
                return;
            }
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
            save();
        }

        EventBus bus = eventBus;
        if (bus != null) {
            bus.post(new ConfigChanged(group, key, oldValue, value));
        }
    }

    public void unsetConfiguration(String group, String key) {
        setConfiguration(group, key, null);
    }

    /** Snapshot of a group, for the in-game settings menu. */
    public synchronized Map<String, String> getGroup(String group) {
        Map<String, String> values = groups.get(group);
        return values == null ? Collections.emptyMap() : Collections.unmodifiableMap(new TreeMap<>(values));
    }

    /**
     * Every config instance created so far, in creation order. The in-game
     * settings menu uses this to list the loaded configs without knowing the
     * plugin/config class mapping.
     */
    public synchronized Map<Class<?>, Object> getConfigs() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(configs));
    }

    /** Clears every explicitly stored value in a group (accessor defaults apply). */
    public void resetGroup(String group) {
        for (String key : getGroup(group).keySet()) {
            setConfiguration(group, key, null);
        }
    }

    public File getFile() {
        return file;
    }

    public synchronized void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("could not create " + parent);
            }
            Files.write(file.toPath(), gson.toJson(groups).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("ConfigManager: could not save " + file + ": " + e);
        }
    }

    private synchronized void load() {
        if (!file.isFile()) {
            return;
        }
        try {
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Map<String, String>>>() { }.getType();
            Map<String, Map<String, String>> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                groups.clear();
                groups.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("ConfigManager: could not read " + file + ": " + e);
        }
    }
}
