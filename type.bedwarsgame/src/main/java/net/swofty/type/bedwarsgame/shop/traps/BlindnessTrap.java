package net.swofty.type.bedwarsgame.shop.traps;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.Trap;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

public class BlindnessTrap extends Trap {

	public BlindnessTrap() {
		super(
			TrapId.BLINDNESS,
				"Blindness Trap",
				ItemStack.of(Material.TRIPWIRE_HOOK),
				"Blinds intruders for 10 seconds.",
				Currency.DIAMOND
		);
	}

	@Override
	public void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer triggerer) {
		triggerer.addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 1, 200));
	}
}
