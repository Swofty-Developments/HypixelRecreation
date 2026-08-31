package net.swofty.type.bedwarsgame.events;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionGameCustomItems implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true)
	public void run(PlayerUseItemOnBlockEvent event) {
		TypeBedWarsGameLoader.getItemHandler().onItemUseOnBlock(event);
	}

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true)
	public void run(PlayerFinishItemUseEvent event) {
		TypeBedWarsGameLoader.getItemHandler().onItemFinishUse(event);
	}

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true)
	public void run(PlayerUseItemEvent event) {
		if (event.getPlayer() instanceof BedWarsPlayer player
			&& player.getGame() != null
			&& player.getGame().getGameType().isOneBlock()
			&& player.getGame().getState() == GameState.IN_PROGRESS
			&& event.getItemStack().material() == Material.VILLAGER_SPAWN_EGG) {
			event.setCancelled(true);
			ItemStack held = event.getItemStack();
			player.setItemInMainHand(held.amount() > 1 ? held.withAmount(held.amount() - 1) : ItemStack.AIR);
			player.getGame().getWorldManager().spawnTemporaryItemShop(player.getPosition());
			return;
		}
		TypeBedWarsGameLoader.getItemHandler().onItemUse(event);
	}

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true)
	public void run(PlayerBlockPlaceEvent event) {
		TypeBedWarsGameLoader.getItemHandler().onBlockPlace(event);
	}

	// InventoryClickItem
	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true)
	public void run(InventoryPreClickEvent event) {
		TypeBedWarsGameLoader.getItemHandler().onInventoryClick(event);
	}

}
