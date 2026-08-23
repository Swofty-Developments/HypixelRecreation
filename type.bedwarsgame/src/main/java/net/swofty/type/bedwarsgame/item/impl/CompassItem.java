package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.gui.GUITrackerAndCommunication;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;

public class CompassItem extends SimpleInteractableItem {

    public CompassItem() {
        super("compass");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.named(Material.COMPASS, Text.key("bedwars.item.compass")).build();
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.openView(new GUITrackerAndCommunication());
    }
}
