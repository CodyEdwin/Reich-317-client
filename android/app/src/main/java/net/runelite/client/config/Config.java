package net.runelite.client.config;

/**
 * Marker interface kept for source compatibility with RuneLite plugin configs.
 *
 * <p>Android configs are concrete classes extending {@link ConfigBase} rather
 * than interfaces with default methods: resolving an interface's default method
 * needs {@code MethodHandles.privateLookupIn}, which Android does not provide.
 */
public interface Config {
}
