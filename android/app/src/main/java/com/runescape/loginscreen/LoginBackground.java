package com.runescape.loginscreen;

/**
 * The login background the player picked.
 *
 * <p>Java port of the desktop Kotlin enum; {@code spriteID} is the cache
 * sprite of the background ({@code -1} for the animated variants).
 */
public enum LoginBackground {

    FADING_BACKGROUNDS(-1),
    ANIMATED_GAME_WORLD(-1),
    NORMAL(4),
    OLD(5),
    CHRISTMAS(6),
    CHAMBERS_OF_XERIC(7),
    DARKMEYER(8),
    DRAGON_SLAYER_2(9),
    FOSSIL_ISLAND(10),
    HALLOWEEN(11),
    HALLOWEEN_2019(12),
    INFERNO(13),
    KEBOS(14),
    MONKEY_MADNESS_2(15),
    PRIFDDINAS(16),
    THEATRE_OF_BLOOD(17),
    A_KINGDOM_DIVIDED(18),
    NEX(19),
    TOMBS_OF_AMASCUT(20);

    private final int spriteID;

    LoginBackground(int spriteID) {
        this.spriteID = spriteID;
    }

    public int getSpriteID() {
        return spriteID;
    }
}
