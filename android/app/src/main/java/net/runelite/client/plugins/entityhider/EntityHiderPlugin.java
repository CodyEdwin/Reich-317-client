package net.runelite.client.plugins.entityhider;

import net.runelite.api.Client;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

/**
 * Android variant of RuneLite's Entity Hider.
 *
 * <p>The client already implements the underlying hide flags
 * ({@code setIsHidingEntities}, {@code setNPCsHidden}, ...), so the plugin is
 * the same thin configuration-to-client bridge as on desktop. No AWT or Swing
 * APIs are involved; only the config class differs (class instead of interface).
 */
@PluginDescriptor(
        name = "Entity Hider",
        description = "Hide players, NPCs, and/or projectiles",
        tags = {"npcs", "players", "projectiles"},
        enabledByDefault = false
)
public class EntityHiderPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private EntityHiderConfig config;

    @Override
    protected void startUp() {
        updateConfig();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (EntityHiderConfig.GROUP.equals(event.getGroup())) {
            updateConfig();
        }
    }

    private void updateConfig() {
        client.setIsHidingEntities(true);

        client.setOthersHidden(config.hideOthers());
        client.setOthersHidden2D(config.hideOthers2D());

        client.setFriendsHidden(config.hideFriends());
        client.setFriendsChatMembersHidden(config.hideFriendsChatMembers());
        client.setIgnoresHidden(config.hideIgnores());

        client.setLocalPlayerHidden(config.hideLocalPlayer());
        client.setLocalPlayerHidden2D(config.hideLocalPlayer2D());

        client.setNPCsHidden(config.hideNPCs());
        client.setNPCsHidden2D(config.hideNPCs2D());

        client.setPetsHidden(config.hidePets());

        client.setAttackersHidden(config.hideAttackers());

        client.setProjectilesHidden(config.hideProjectiles());
    }

    @Override
    protected void shutDown() {
        client.setIsHidingEntities(false);

        client.setOthersHidden(false);
        client.setOthersHidden2D(false);

        client.setFriendsHidden(false);
        client.setFriendsChatMembersHidden(false);
        client.setIgnoresHidden(false);

        client.setLocalPlayerHidden(false);
        client.setLocalPlayerHidden2D(false);

        client.setNPCsHidden(false);
        client.setNPCsHidden2D(false);

        client.setPetsHidden(false);

        client.setAttackersHidden(false);

        client.setProjectilesHidden(false);
    }
}
