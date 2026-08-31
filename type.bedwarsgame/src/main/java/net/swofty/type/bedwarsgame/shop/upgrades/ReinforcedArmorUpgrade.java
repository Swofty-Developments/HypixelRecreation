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

public class ReinforcedArmorUpgrade extends TeamUpgrade {

	public ReinforcedArmorUpgrade() {
		super(
			TeamUpgradeId.REINFORCED_ARMOR,
				"Reinforced Armor",
				"Your team permanently gains Protection on all armor pieces!",
				ItemStack.of(Material.IRON_CHESTPLATE),
				List.of(
						new TeamUpgradeTier(1, "Protection I", 2, Currency.DIAMOND),
						new TeamUpgradeTier(2, "Protection II", 4, Currency.DIAMOND),
						new TeamUpgradeTier(3, "Protection III", 8, Currency.DIAMOND),
						new TeamUpgradeTier(4, "Protection IV", 16, Currency.DIAMOND)
				)
		);
	}

	public static void enchantArmor(Player player, int level) {
		player.setBoots(player.getBoots().with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, level)));
		player.setLeggings(player.getLeggings().with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, level)));
		player.setChestplate(player.getChestplate().with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, level)));
		player.setHelmet(player.getHelmet().with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, level)));
	}

	@Override
	public void applyEffect(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, int level) {
		game.getPlayers().stream()
				.filter(p -> teamName.equals(p.getTeamKey()))
				.forEach(player -> enchantArmor(player, level));
	}
}
