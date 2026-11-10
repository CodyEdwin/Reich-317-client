package net.runelite.client.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a single configuration entry. On the desktop this annotates the
 * default method of a config interface; on Android it annotates the accessor of
 * a {@link ConfigBase} subclass (the in-game settings menu reads these).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigItem {
    int position() default -1;

    String keyName();

    String name();

    String description();

    boolean hidden() default false;

    String warning() default "";

    boolean secret() default false;

    String section() default "";

    /**
     * Marks the item as a colour. The value is an ARGB int stored as
     * {@code AARRGGBB} hex; the Android settings editor shows a colour picker
     * instead of a number field.
     */
    boolean color() default false;
}
