package net.runelite.client.eventbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event subscriber. Android keeps RuneLite's contract:
 * one parameter (the event), void return, instance method, named
 * {@code on<EventSimpleName>}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Subscribe {
    /**
     * Higher priority subscribers run first.
     */
    float priority() default 0;
}
