package com.runescape.loginscreen.worlds;

/**
 * One entry of the world list.
 *
 * <p>Java port of the desktop Kotlin data class: the accessors keep the
 * desktop names ({@code getIp()}, {@code getName()}, {@code getType()},
 * {@code getDescription()}).
 */
public class WorldData {

    private final String ip;

    private final String name;

    private final WorldType type;

    private final String description;

    public WorldData(String ip, String name, WorldType type) {
        this(ip, name, type, "-");
    }

    public WorldData(String ip, String name, WorldType type, String description) {
        this.ip = ip;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public WorldType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "WorldData(ip=" + ip + ", name=" + name + ", type=" + type + ")";
    }
}
