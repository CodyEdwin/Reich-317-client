package net.runelite.client.config;

/**
 * Base class for Android plugin configurations.
 *
 * <p>Values are read through the helpers below, which consult
 * {@link ConfigManager} (persisted as JSON in the client's data directory) and
 * fall back to the default supplied by the accessor. This replaces RuneLite's
 * interface + default-method + dynamic-proxy design, which relies on Java 9
 * {@code MethodHandles} APIs that Android does not have.
 */
public abstract class ConfigBase implements Config {

    private ConfigManager configManager;

    public final void attach(ConfigManager configManager) {
        this.configManager = configManager;
    }

    protected final ConfigManager configManager() {
        return configManager;
    }

    /** The group this config is stored under ({@link ConfigGroup} or the class name). */
    public final String group() {
        ConfigGroup group = getClass().getAnnotation(ConfigGroup.class);
        return group != null ? group.value() : getClass().getName();
    }

    protected final boolean getBoolean(String key, boolean defaultValue) {
        return configManager.getBoolean(group(), key, defaultValue);
    }

    protected final int getInt(String key, int defaultValue) {
        return configManager.getInt(group(), key, defaultValue);
    }

    protected final double getDouble(String key, double defaultValue) {
        return configManager.getDouble(group(), key, defaultValue);
    }

    protected final String getString(String key, String defaultValue) {
        return configManager.getConfiguration(group(), key, defaultValue);
    }

    /**
     * An ARGB colour, stored as {@code AARRGGBB} hex so the value survives the
     * JSON store unchanged.
     */
    protected final int getColor(String key, int defaultValue) {
        String value = configManager.getConfiguration(group(), key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (int) Long.parseLong(value.startsWith("#") ? value.substring(1) : value, 16);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Stores an ARGB colour as {@code AARRGGBB} hex. */
    protected final void setColor(String key, int argb) {
        set(key, String.format("%08X", argb & 0xFFFFFFFFL));
    }

    /**
     * A keybind, stored as {@code keyCode:modifiers}.
     */
    protected final Keybind getKeybind(String key, Keybind defaultValue) {
        String value = configManager.getConfiguration(group(), key);
        if (value == null) {
            return defaultValue;
        }
        int separator = value.indexOf(':');
        if (separator < 0) {
            return defaultValue;
        }
        try {
            return new Keybind(Integer.parseInt(value.substring(0, separator)),
                    Integer.parseInt(value.substring(separator + 1)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected final void setKeybind(String key, Keybind keybind) {
        set(key, keybind.getKeyCode() + ":" + keybind.getModifiers());
    }

    protected final <T extends Enum<T>> T getEnum(String key, T defaultValue) {        String value = configManager.getConfiguration(group(), key);
        if (value == null) {
            return defaultValue;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) defaultValue.getDeclaringClass();
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    protected final void set(String key, Object value) {
        configManager.setConfiguration(group(), key, String.valueOf(value));
    }
}
