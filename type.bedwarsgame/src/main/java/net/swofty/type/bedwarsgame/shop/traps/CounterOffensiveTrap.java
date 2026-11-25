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

public class CounterOffensiveTrap extends Trap {

	public CounterOffensiveTrap() {
		super(
			TrapId.COUNTER_OFFENSIVE,
				"Counter-Offensive Trap",
				ItemStack.of(Material.FEATHER),
				"Grants Speed II to your team for 10s when onTrigger.",
				Currency.DIAMOND
		);
	}

	@Override
	public void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer triggerer) {
		game.getPlayers().stream()
				.filter(p -> teamName.equals(p.getTeamKey()))
				.forEach(p -> p.addEffect(new Potion(PotionEffect.SPEED, (byte) 1, 200)));
	}
}
