package net.swofty.type.bedwarsgame.item.impl;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import net.swofty.type.generic.utility.ScheduleUtility;

public class Fireball extends SimpleInteractableItem {

	public Fireball() {
		super("fireball", new ShopData("Fireball", "Right-click to launch! Great to knock\nback enemies walking on thin bridges.",
			40, 1, Currency.IRON, DatapointBedWarsHotbar.HotbarItemType.UTILITY, 7));
	}


	@Override
	public ItemStack getBlandItem() {
		return ItemStack.of(Material.FIRE_CHARGE);
	}

	@Override
	public void onItemUse(PlayerUseItemEvent event) {
		handleFireball(event.getPlayer(), event.getHand());
	}

	@Override
	public void onItemUseOnBlock(PlayerUseItemOnBlockEvent event) {
		handleFireball(event.getPlayer(), event.getHand());
	}

	private void handleFireball(Player player, PlayerHand hand) {
		player.setItemInHand(hand, player.getItemInHand(hand).withAmount(player.getItemInHand(hand).amount() - 1));
        Polyp.getInstance().services().projectiles().launch(ProjectileSnapshot.of(player,
                io.github.term4.polyp.mechanics.projectile.types.Fireball.INSTANCE));
		player.playSound(Sound.sound(Key.key("minecraft:entity.ghast.shoot"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());
		player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(Key.key("bw:fireball"), -0.3, AttributeOperation.ADD_MULTIPLIED_TOTAL));
		ScheduleUtility.delay(() -> player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(Key.key("bw:fireball")), TaskSchedule.seconds(2));
	}
}
