package com.elvarg.android.beans;

/**
 * Android-neutral stand-in for JavaFX's {@code ObservableValue}, used by the
 * login screen's fade animation.
 *
 * <p>The shared login screen was written against a couple of JavaFX property
 * classes ({@code DoubleProperty}/{@code SimpleDoubleProperty}/{@code
 * ObservableValue}). JavaFX is not available on Android, so these tiny
 * equivalents provide just the observed-property behaviour the login screen
 * needs.
 */
public interface ObservableValue<T> {
    T getValue();
}
