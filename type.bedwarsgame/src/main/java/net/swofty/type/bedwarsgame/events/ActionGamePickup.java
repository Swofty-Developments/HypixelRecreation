package net.swofty.type.bedwarsgame.events;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.user.ExperienceCause;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import org.jetbrains.annotations.Nullable;

public class ActionGamePickup implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
	public void run(PickupItemEvent event) {
		ItemStack itemStack = event.getItemEntity().getItemStack();
		if (event.getLivingEntity() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (!game.isPlayerCurrentlyPlaying(player.getUuid())) {
				event.setCancelled(true);
				return;
			}

			// Record item pickup to replay
			if (game != null && game.getReplayManager().isRecording()) {
				game.getReplayManager().recordItemPickup(
					event.getItemEntity().getEntityId(),
					player.getEntityId()
				);
			}

			player.getInventory().addItemStack(itemStack);
			if (isSplittableGeneratorResource(itemStack)) {
				game.getPlayers().stream()
						.filter(teammate -> teammate != player)
						.filter(teammate -> teammate.getTeamKey() == player.getTeamKey())
						.filter(teammate -> teammate.getInstance() == player.getInstance())
						.filter(teammate -> teammate.getPosition().distanceSquared(player.getPosition()) <= 4)
						.forEach(teammate -> teammate.getInventory().addItemStack(itemStack));
			}

			// handle bedwars xp for diamonds and emeralds
			for (Currency currency : Currency.values()) {
				if (itemStack.material().equals(currency.getMaterial())) {
					@Nullable CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
					if (data != null && !data.nbt().getBoolean("generator")) continue;
					int amount = itemStack.amount();
					switch (currency) {
						case DIAMOND:
							player.xp(ExperienceCause.DIAMONDS, amount);
							break;
						case EMERALD:
							player.xp(ExperienceCause.EMERALDS, amount);
							break;
					}
					break;
				}
			}
		}
	}

	private boolean isSplittableGeneratorResource(ItemStack itemStack) {
		if (itemStack.material() != net.minestom.server.item.Material.IRON_INGOT
				&& itemStack.material() != net.minestom.server.item.Material.GOLD_INGOT) return false;
		CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.nbt().getBoolean("generator");
	}

}
