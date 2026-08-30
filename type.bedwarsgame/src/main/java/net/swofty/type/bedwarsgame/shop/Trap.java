package net.swofty.type.bedwarsgame.shop;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

import java.util.Set;

@Getter
public abstract class Trap {
	private final TrapId id;
	private final String name;
	private final ItemStack displayItem;
	private final String description;
	private final Currency currency;

	public Trap(TrapId id, String name, ItemStack displayItem, String description, Currency currency) {
		this.id = id;
		this.name = name;
		this.displayItem = displayItem;
		this.description = description;
		this.currency = currency;
	}

	/**
	 * Triggers the trap for the specified team.
	 *
	 * @param game      the game instance
	 * @param teamKey   the key of the team that owns the trap
	 * @param triggerer the player who triggered the trap
	 */
	public void trigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey, BedWarsPlayer triggerer) {
		game.getTeam(teamKey.name()).ifPresent(team -> team.removeTrap(id));

		// Notify all team members
		game.getPlayers().stream()
				.filter(p -> teamKey.equals(p.getTeamKey()))
				.forEach(player -> {
					player.sendTitlePart(TitlePart.TITLE, "<c><l>TRAP TRIGGERED");
					player.playSound(Sound.sound(Key.key("minecraft:entity.enderman.death"), Sound.Source.MASTER, 1.0f, 0.6f));
				});
		onTrigger(game, teamKey, triggerer);
	}

	/**
	 * Abstract method to define the trap's behavior when triggered.
	 *
	 * @param game      the game instance
	 * @param teamKey   the key of the team that owns the trap
	 * @param triggerer the player who triggered the trap
	 */
	public abstract void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey, BedWarsPlayer triggerer);

	/**
	 * Gets the price of the trap for the specified team.
	 *
	 * @param game     the game instance
	 * @param teamKey  the key of the team that owns the trap
	 * @return the price of the trap
	 */
	public int getPrice(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey) {
		return game.getTeam(teamKey.name())
				.map(team -> team.getTrapCount() + 1)
				.orElse(1);
	}

	public ItemStack getDisplayItem() {
		return displayItem.with(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, Set.of()));
	}
}
