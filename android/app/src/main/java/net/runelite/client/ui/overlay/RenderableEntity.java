package net.runelite.client.ui.overlay;

import com.elvarg.android.geom.Dimension;
import com.elvarg.android.gfx.Graphics2D;

/**
 * Something that can be drawn onto an overlay and reports the space it used.
 */
public interface RenderableEntity {

    Dimension render(Graphics2D graphics);
}
