package net.runelite.client.ui.overlay;

import net.runelite.api.MenuAction;

/**
 * An entry of an overlay's right-click menu.
 *
 * <p>Same shape as the desktop class: the menu action that is sent to the
 * client, the option text and the target it applies to.
 */
public class OverlayMenuEntry {

    private final MenuAction menuAction;

    private final String option;

    private final String target;

    public OverlayMenuEntry(MenuAction menuAction, String option, String target) {
        this.menuAction = menuAction;
        this.option = option;
        this.target = target;
    }

    public MenuAction getMenuAction() {
        return menuAction;
    }

    public String getOption() {
        return option;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return option + (target == null ? "" : " (" + target + ")");
    }
}
