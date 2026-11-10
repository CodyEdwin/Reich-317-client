package com.elvarg.android;

/**
 * The scaling options of the game view.
 *
 * <p>The desktop <em>Stretched Mode</em> plugin resizes the AWT canvas that
 * holds the game; on Android the client always renders into a fixed
 * 765x503 framebuffer and {@link GameView} scales that framebuffer to the
 * window. The plugin therefore publishes its options here and the view picks
 * them up every frame.
 *
 * <p>Written in Java; the values are read from the UI thread and written from
 * the game thread, hence the volatile fields.
 */
public final class ViewScaling {

    private static volatile boolean keepAspectRatio = true;

    private static volatile boolean integerScaling;

    private static volatile boolean increasedPerformance;

    private static volatile int scalingFactor = 100;

    private ViewScaling() {
    }

    /**
     * Whether the game keeps its 765x503 aspect ratio (letterboxed) instead of
     * being stretched to fill the window.
     */
    public static boolean isKeepAspectRatio() {
        return keepAspectRatio;
    }

    public static void setKeepAspectRatio(boolean keepAspectRatio) {
        ViewScaling.keepAspectRatio = keepAspectRatio;
    }

    /** Whether the scale factor is rounded down to a whole number. */
    public static boolean isIntegerScaling() {
        return integerScaling;
    }

    public static void setIntegerScaling(boolean integerScaling) {
        ViewScaling.integerScaling = integerScaling;
    }

    /** Whether the framebuffer is scaled without filtering (faster, blockier). */
    public static boolean isIncreasedPerformance() {
        return increasedPerformance;
    }

    public static void setIncreasedPerformance(boolean increasedPerformance) {
        ViewScaling.increasedPerformance = increasedPerformance;
    }

    /** How much of the screen the game may use, in percent (100 = full). */
    public static int getScalingFactor() {
        return scalingFactor;
    }

    public static void setScalingFactor(int scalingFactor) {
        ViewScaling.scalingFactor = Math.max(10, Math.min(100, scalingFactor));
    }
}
