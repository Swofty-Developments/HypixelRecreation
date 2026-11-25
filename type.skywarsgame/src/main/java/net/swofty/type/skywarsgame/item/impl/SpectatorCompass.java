package net.swofty.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.gui.GUISpectatorTeleporter;
import net.swofty.type.skywarsgame.item.SimpleInteractableItem;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;

public class SpectatorCompass extends SimpleInteractableItem {

    public SpectatorCompass() {
        super("spectator_compass");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStackCreator.item(Material.COMPASS, 1, """
                <a>Teleporter <7>(Right Click)
                <7>Right-click to teleport to players!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game != null) {
            new GUISpectatorTeleporter(game).open(player);
        }
    }
}
