package net.runelite.client.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Units shown next to a config item's value.
 *
 * <p>Same constants as the desktop annotation; the Android settings editor
 * appends the unit to the value label.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Units {

    String MILLISECONDS = "ms";
    String MINUTES = " mins";
    String PERCENT = "%";
    String PIXELS = "px";
    String SECONDS = "s";
    String TICKS = " ticks";

    String value();
}
