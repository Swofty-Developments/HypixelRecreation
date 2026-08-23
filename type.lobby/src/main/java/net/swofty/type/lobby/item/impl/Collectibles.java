package net.swofty.type.lobby.item.impl;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.item.LobbyItem;

public class Collectibles extends LobbyItem {

	public Collectibles() {
		super("collectibles");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.named(Material.CHEST, "<a>Collectibles <7>(Right Click)").build();
	}

	@Override
	public void onItemDrop(ItemDropEvent event) {
		event.setCancelled(true);
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		((CancellableEvent) event).setCancelled(true);
		((HypixelPlayer) event.getPlayer()).notImplemented();
	}

}
