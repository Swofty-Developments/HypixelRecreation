package net.swofty.type.bedwarsgame.shop.traps;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.Trap;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

public class MinerFatigueTrap extends Trap {

	public MinerFatigueTrap() {
		super(
			TrapId.MINER_FATIGUE,
				"Miner Fatigue Trap",
				ItemStack.of(Material.IRON_PICKAXE),
				"Inflicts Mining Fatigue II for 10s.",
				Currency.DIAMOND
		);
	}

	@Override
	public void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer triggerer) {
		triggerer.getAttribute(Attribute.MINING_EFFICIENCY).addModifier(new AttributeModifier(Key.key("bw:miner_fatigue"), -0.6, AttributeOperation.ADD_MULTIPLIED_TOTAL));
	}
}
