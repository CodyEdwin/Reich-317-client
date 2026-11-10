package net.runelite.client.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PluginDescriptor {
    String name();

    /** Internal name used in the config. */
    String configName() default "";

    String description() default "";

    String[] tags() default {};

    String[] conflicts() default {};

    boolean enabledByDefault() default true;

    boolean hidden() default false;

    boolean developerPlugin() default false;

    boolean loadWhenOutdated() default false;

    boolean loadInSafeMode() default true;
}
