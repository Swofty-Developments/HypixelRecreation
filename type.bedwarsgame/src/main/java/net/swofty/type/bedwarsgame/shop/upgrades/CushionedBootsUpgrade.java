package net.swofty.type.bedwarsgame.shop.upgrades;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
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

public class CushionedBootsUpgrade extends TeamUpgrade {
	public CushionedBootsUpgrade() {
		super(
			TeamUpgradeId.CUSHIONED_BOOTS,
				"Cushioned Boots",
				"Your team permanently gains Feather Falling enchantment on boots.",
				ItemStack.of(Material.IRON_BOOTS),
				List.of(
						new TeamUpgradeTier(1, "Feather Falling I", 2, Currency.DIAMOND),
						new TeamUpgradeTier(2, "Feather Falling II", 4, Currency.DIAMOND)
				)
		);
	}

	@Override
	public void applyEffect(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, int level) {
		game.getPlayers().stream()
				.filter(p -> teamName.equals(p.getTeamKey()))
				.forEach(player -> {
					var boots = player.getBoots();
					if (boots == null) return;

					EnchantmentList list = boots.get(DataComponents.ENCHANTMENTS);
					if (list == null) {
						list = EnchantmentList.EMPTY;
					}

					player.setBoots(
							boots.with(DataComponents.ENCHANTMENTS, list.with(Enchantment.FEATHER_FALLING, level))
					);

					player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER).addModifier(
							new AttributeModifier("bw:cushioned_boots", -0.1 * level, AttributeOperation.ADD_MULTIPLIED_TOTAL)
					);
				});
	}
}
