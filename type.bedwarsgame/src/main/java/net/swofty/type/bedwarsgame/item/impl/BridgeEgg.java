package net.swofty.type.bedwarsgame.item.impl;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.types.Egg;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import net.swofty.type.generic.utility.ScheduleUtility;

import java.util.concurrent.ThreadLocalRandom;

public class BridgeEgg extends SimpleInteractableItem {
	public BridgeEgg() {
		super("bridge_egg", new ShopData("Bridge Egg", "This egg creates a bridge in its trail\nafter being thrown.",
			1, 1, Currency.EMERALD, DatapointBedWarsHotbar.HotbarItemType.UTILITY, 7));
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStack.of(Material.EGG);
	}

	@Override
	public void onItemUse(PlayerUseItemEvent event) {
		BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
		if (teamKey == null) return;

		ItemStack stack = event.getItemStack();
		Block woolBlock = teamKey.bedMaterial().block();

		ThreadLocalRandom random = ThreadLocalRandom.current();
		player.getViewersAsAudience().playSound(Sound.sound(
				SoundEvent.ENTITY_EGG_THROW,
				Sound.Source.PLAYER,
				0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
		), player);

		var projectile = Polyp.getInstance().services().projectiles().launch(
				ProjectileSnapshot.of(player, Egg.INSTANCE)
						.withItem(stack)
						.withPower(1.5)
						.withBehavior(new ProjectileBehavior() {
							@Override
							public void onTick(ManagedProjectile egg, long time) {
								Vec velocity = egg.velocityBt();
								double length = Math.hypot(velocity.x(), velocity.z());
								if (length == 0 || egg.getInstance() == null) return;
								Point center = egg.getPosition().sub(0, 1, 0).add(-velocity.x() / length, 0, -velocity.z() / length);
								for (int x = -1; x <= 0; x++)
									for (int z = -1; z <= 0; z++)
										egg.getInstance().setBlock(center.add(x, 0, z), woolBlock);
							}
						}));

		if (player.getGameMode() != GameMode.CREATIVE) {
			player.setItemInHand(event.getHand(), stack.withAmount(stack.amount() - 1));
		}

		ScheduleUtility.delay(
				() -> {
					if (projectile != null && projectile.isActive() && !projectile.isRemoved()) {
						projectile.remove();
					}
				},
				TaskSchedule.seconds(4)
		);
	}
}
