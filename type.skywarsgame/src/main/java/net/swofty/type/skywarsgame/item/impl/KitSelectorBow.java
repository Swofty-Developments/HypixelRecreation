package net.swofty.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.gui.GUICageKitSelector;
import net.swofty.type.skywarsgame.item.SimpleInteractableItem;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;

public class KitSelectorBow extends SimpleInteractableItem {

    public KitSelectorBow() {
        super("kit_selector");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStackCreator.item(Material.BOW, 1, """
                <a>Kit Selector <7>(Right Click)
                <7>Right-click to select your kit!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(event.getPlayer());
        if (game != null && game.getState().isWaiting()) {
            SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
            String mode = game.getGameType().getModeString();
            new GUICageKitSelector(mode, game).open(player);
        }
    }
}
