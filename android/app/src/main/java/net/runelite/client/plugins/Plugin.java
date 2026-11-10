package net.runelite.client.plugins;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Base class for RuneLite plugins on Android.
 *
 * <p>Kept source-compatible with the desktop class (same lifecycle methods and
 * {@link Module} contract) so plugin code ports with minimal changes. On Android
 * dependencies are provided by {@code com.elvarg.android.rl.AndroidInjector}
 * rather than a Guice injector; {@link #getInjector()} returns {@code null}
 * unless a host installs one.
 */
public abstract class Plugin implements Module {

    protected Injector injector;

    @Override
    public void configure(Binder binder) {
    }

    protected void startUp() throws Exception {
    }

    protected void shutDown() throws Exception {
    }

    public void resetConfiguration() {
    }

    public final Injector getInjector() {
        return injector;
    }

    public String getName() {
        PluginDescriptor descriptor = getClass().getAnnotation(PluginDescriptor.class);
        return descriptor != null ? descriptor.name() : getClass().getSimpleName();
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
