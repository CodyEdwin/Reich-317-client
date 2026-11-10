package net.runelite.client.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Groups config items into a named section.
 *
 * <p>On the desktop this annotates a constant field; the Android settings
 * editor groups the items that reference the section's key.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigSection {

    String name();

    String description();

    int position();

    boolean closedByDefault() default false;
}
