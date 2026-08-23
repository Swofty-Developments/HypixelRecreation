package net.swofty.type.lobby.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.LobbyTypeLoader;
import net.swofty.type.lobby.item.LobbyItem;
import net.swofty.type.lobby.parkour.LobbyParkourManager;

public class ResetParkour extends LobbyItem {

	public ResetParkour() {
		super("reset_parkour");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.named(Material.OAK_DOOR, "<c>Reset").build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		((CancellableEvent) event).setCancelled(true);
		if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
			LobbyParkourManager parkourManager = lobbyLoader.getParkourManager();
			if (parkourManager == null) return;
			parkourManager.resetPlayer((HypixelPlayer) event.getPlayer());
		}
	}
}
