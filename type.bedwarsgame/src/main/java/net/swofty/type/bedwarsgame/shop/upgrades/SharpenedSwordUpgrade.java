package net.swofty.type.bedwarsgame.shop.upgrades;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.TeamUpgrade;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeTier;

import java.util.List;

public class SharpenedSwordUpgrade extends TeamUpgrade {

	public SharpenedSwordUpgrade() {
		super(
			TeamUpgradeId.SHARPNESS,
				"Sharpened Swords",
				"Your team permanently gains Sharpness on all swords and axes!",
				ItemStack.of(Material.IRON_SWORD),
				List.of(
						new TeamUpgradeTier(1, "Sharpness I", 4, Currency.DIAMOND)
				)
		);
	}

	public static void enchantItems(Player player, int level) {
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack stack = player.getInventory().getItemStack(i);
			if (stack.material().name().endsWith("_sword") || stack.material().name().endsWith("_axe")) {
				player.getInventory().setItemStack(i, stack.with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, level)));
			}
		}
	}

	@Override
	public void applyEffect(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, int level) {
		game.getPlayers().stream()
				.filter(p -> teamName.equals(p.getTeamKey()))
				.forEach(player -> enchantItems(player, level));
	}
}
