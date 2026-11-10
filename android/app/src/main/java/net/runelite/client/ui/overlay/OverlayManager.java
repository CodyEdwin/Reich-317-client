package net.runelite.client.ui.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holds the registered overlays. Rendering is done by {@link OverlayRenderer}.
 */
public final class OverlayManager {

    private static final Comparator<Overlay> BY_PRIORITY =
            Comparator.comparing(Overlay::getPriority).reversed();

    private final List<Overlay> overlays = new CopyOnWriteArrayList<>();

    public void add(Overlay overlay) {
        if (overlay != null) {
            overlays.add(overlay);
        }
    }

    public void addAll(Overlay... overlays) {
        for (Overlay overlay : overlays) {
            add(overlay);
        }
    }

    public void remove(Overlay overlay) {
        overlays.remove(overlay);
    }

    public void removeAll(Overlay... overlays) {
        for (Overlay overlay : overlays) {
            remove(overlay);
        }
    }

    /** True when no overlays are registered; lets the draw hook skip its work. */
    public boolean isEmpty() {
        return overlays.isEmpty();
    }

    public List<Overlay> getOverlays() {
        List<Overlay> sorted = new ArrayList<>(overlays);
        sorted.sort(BY_PRIORITY);
        return Collections.unmodifiableList(sorted);
    }

    public List<Overlay> getOverlays(OverlayLayer layer) {
        List<Overlay> sorted = new ArrayList<>();
        for (Overlay overlay : overlays) {
            if (overlay.getLayer() == layer) {
                sorted.add(overlay);
            }
        }
        sorted.sort(BY_PRIORITY);
        return sorted;
    }

    /**
     * The topmost overlay that was drawn at the given canvas position, or
     * {@code null}. Used to route a right click to the overlay under the
     * cursor.
     */
    public Overlay getOverlayAt(int x, int y) {
        List<Overlay> sorted = getOverlays();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Overlay overlay = sorted.get(i);
            com.elvarg.android.geom.Rectangle bounds = overlay.getBounds();
            if (bounds != null && bounds.width > 0 && bounds.height > 0 && bounds.contains(x, y)) {
                return overlay;
            }
        }
        return null;
    }
}
