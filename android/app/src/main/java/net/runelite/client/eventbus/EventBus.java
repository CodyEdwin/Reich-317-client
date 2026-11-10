package net.runelite.client.eventbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Android implementation of RuneLite's event bus.
 *
 * <p>The desktop version uses {@code LambdaMetafactory} to build fast
 * {@code Consumer} invokers. Android's {@code java.lang.invoke} support is
 * limited (API 26+ only, partial), so this implementation always dispatches
 * through {@link Method#invoke}. The public API is unchanged: register/unregister
 * objects, subscribe with {@link Subscribe}, post by exact event class, ordered
 * by priority then subscriber class name.
 */
public class EventBus {

    public static class Subscriber {
        private final Object object;
        private final Method method;
        private final float priority;

        public Subscriber(Object object, Method method, float priority) {
            this.object = object;
            this.method = method;
            this.priority = priority;
        }

        public Object getObject() {
            return object;
        }

        public Method getMethod() {
            return method;
        }

        public float getPriority() {
            return priority;
        }

        void invoke(Object arg) throws Exception {
            method.invoke(object, arg);
        }

        @Override
        public String toString() {
            return object.getClass().getSimpleName() + "." + method.getName() + "(" + priority + ")";
        }
    }

    private static final Comparator<Subscriber> ORDER = Comparator
            .comparingDouble(Subscriber::getPriority)
            .reversed()
            .thenComparing(s -> s.getObject().getClass().getName());

    private final Consumer<Throwable> exceptionHandler;

    private volatile List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public EventBus() {
        this(t -> t.printStackTrace());
    }

    public EventBus(Consumer<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Subscribes every {@link Subscribe} method declared by {@code object} (and
     * its superclasses). Re-registering an object replaces its subscriptions.
     */
    public synchronized void register(Object object) {
        unregister(object);

        List<Subscriber> added = new ArrayList<>();
        for (Class<?> clazz = object.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                Subscribe sub = method.getAnnotation(Subscribe.class);
                if (sub == null) {
                    continue;
                }
                if (method.getReturnType() != Void.TYPE
                        || method.getParameterCount() != 1
                        || Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("Invalid @Subscribe method " + method
                            + ": must be a non-static void method with exactly one parameter");
                }

                Class<?> eventType = method.getParameterTypes()[0];
                if (eventType.isPrimitive() || eventType.isInterface()
                        || Modifier.isAbstract(eventType.getModifiers())) {
                    throw new IllegalArgumentException("Invalid @Subscribe method " + method
                            + ": event type must be a concrete class");
                }

                String expected = "on" + eventType.getSimpleName();
                if (!method.getName().equals(expected)) {
                    throw new IllegalArgumentException("Subscribed method " + method
                            + " should be named " + expected);
                }

                method.setAccessible(true);
                added.add(new Subscriber(object, method, sub.priority()));
            }
        }

        if (!added.isEmpty()) {
            List<Subscriber> updated = new ArrayList<>(subscribers);
            updated.addAll(added);
            updated.sort(ORDER);
            subscribers = updated;
        }
    }

    public synchronized void unregister(Object object) {
        List<Subscriber> updated = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            if (subscriber.getObject() != object) {
                updated.add(subscriber);
            }
        }
        subscribers = updated;
    }

    /** Posts an event to every subscriber registered for exactly this class. */
    public void post(Object event) {
        if (event == null) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            if (!subscriber.getMethod().getParameterTypes()[0].isInstance(event)) {
                continue;
            }
            try {
                subscriber.invoke(event);
            } catch (Throwable t) {
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                exceptionHandler.accept(cause);
            }
        }
    }

    /**
     * RuneLite's event bus has no deferred queue of its own (the desktop
     * {@code DeferredEventBus} is a separate class); this keeps parity for
     * callers that expect the method.
     */
    public void postDeferred(Object event) {
        post(event);
    }
}
