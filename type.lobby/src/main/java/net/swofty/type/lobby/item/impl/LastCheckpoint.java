package net.swofty.type.lobby.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.lobby.LobbyTypeLoader;
import net.swofty.type.lobby.item.LobbyItem;
import net.swofty.type.lobby.parkour.LobbyParkourManager;

public class LastCheckpoint extends LobbyItem {

	public LastCheckpoint() {
		super("last_checkpoint");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.named(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, "<a>Teleport to Last Checkpoint").build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		((CancellableEvent) event).setCancelled(true);
		if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
			LobbyParkourManager parkourManager = lobbyLoader.getParkourManager();
			if (parkourManager == null) return;
			event.getPlayer().teleport(
					parkourManager.getParkour().getCheckpoints().get(parkourManager.getPerPlayerStartTime().get(event.getPlayer().getUuid()).lastCheckpointIndex()).asPos().add(0.5, 0, 0.5)
			);
		}
	}
}
