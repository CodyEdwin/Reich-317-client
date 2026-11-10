package com.runescape.loginscreen;

/**
 * The state of the login screen.
 *
 * <p>Java port of the desktop Kotlin enum (the desktop sources are untouched;
 * the Android module ships this variant instead).
 */
public enum LoginState {
    EULA,
    LOGIN,
    WORLD_SELECT,
    WELCOME
}
