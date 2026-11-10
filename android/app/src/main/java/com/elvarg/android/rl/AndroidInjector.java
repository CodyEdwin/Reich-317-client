package com.elvarg.android.rl;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Minimal dependency injector for the Android RuneLite host.
 *
 * <p>RuneLite uses Guice, including per-plugin child injectors and Guice-backed
 * config proxies. Both are awkward on Android (dynamic proxies for config
 * interfaces, {@code MethodHandles} for default methods), so the Android host
 * uses this small reflection-based injector instead: it resolves constructor and
 * field dependencies from an explicit service map, creates
 * {@link ConfigBase} subclasses through {@link ConfigManager}, and caches
 * created instances.
 */
public final class AndroidInjector {

    private final Map<Class<?>, Object> services = new LinkedHashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final ConfigManager configManager;

    public AndroidInjector(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /** Binds a service instance under {@code type} (an interface or class). */
    public <T> void bind(Class<T> type, T instance) {
        services.put(type, instance);
    }

    /** Resolves a service, config or injectable component. */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getInstance(Class<T> type) {
        Object service = resolve(type);
        if (service != null) {
            return (T) service;
        }
        if (ConfigBase.class.isAssignableFrom(type)) {
            return type.cast(configManager.getConfig(type));
        }
        Object existing = instances.get(type);
        if (existing != null) {
            return (T) existing;
        }
        T created = instantiate(type);
        injectMembers(created);
        instances.put(type, created);
        return created;
    }

    /** Injects {@code @Inject} fields of {@code target}, including superclasses. */
    public void injectMembers(Object target) {
        for (Class<?> clazz = target.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Inject.class) == null || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(target, getInstance(field.getType()));
                } catch (ReflectiveOperationException | RuntimeException e) {
                    throw new IllegalStateException("Cannot inject " + clazz.getName() + "." + field.getName(), e);
                }
            }
        }
    }

    private Object resolve(Class<?> type) {
        Object service = services.get(type);
        if (service != null) {
            return service;
        }
        for (Object candidate : services.values()) {
            if (type.isInstance(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = findConstructor(type);
            constructor.setAccessible(true);
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] arguments = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                arguments[i] = getInstance(parameters[i]);
            }
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create " + type.getName(), e);
        }
    }

    private static <T> Constructor<T> findConstructor(Class<T> type) throws NoSuchMethodException {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        Constructor<?> fallback = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                @SuppressWarnings("unchecked")
                Constructor<T> injected = (Constructor<T>) constructor;
                return injected;
            }
            if (constructor.getParameterCount() == 0) {
                @SuppressWarnings("unchecked")
                Constructor<T> empty = (Constructor<T>) constructor;
                fallback = empty;
            }
        }
        if (fallback != null) {
            @SuppressWarnings("unchecked")
            Constructor<T> empty = (Constructor<T>) fallback;
            return empty;
        }
        if (constructors.length == 1) {
            @SuppressWarnings("unchecked")
            Constructor<T> only = (Constructor<T>) constructors[0];
            return only;
        }
        throw new NoSuchMethodException(type.getName() + " has no @Inject or no-argument constructor");
    }
}
