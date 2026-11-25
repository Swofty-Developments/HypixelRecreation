package net.swofty.type.bedwarsgame.shop.upgrades;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.shop.TeamUpgrade;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeTier;
import net.swofty.type.game.game.GameState;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.List;

public class ForgeUpgrade extends TeamUpgrade {
	public ForgeUpgrade() {
		super(
			TeamUpgradeId.FORGE,
				"Forge Upgrade",
				"Upgrade resource spawning on your island.",
				ItemStack.of(Material.FURNACE),
				List.of(
						new TeamUpgradeTier(1, "+50% Resources", 2, Currency.DIAMOND),
						new TeamUpgradeTier(2, "+100% Resources", 4, Currency.DIAMOND),
						new TeamUpgradeTier(3, "Spawn emeralds", 6, Currency.DIAMOND),
						new TeamUpgradeTier(4, "+200% Resources", 8, Currency.DIAMOND)
				)
		);
	}

	@Override
	public void applyEffect(BedWarsGame game, TeamKey teamKey, int level) {
		// The resource multiplier for iron/gold is handled passively by the generator task in Game.java.
		// This method only needs to handle the active effect of starting the emerald generator at level 3.
		if (level != 3) {
			return;
		}

		MapTeam team = teamKey != null
				? game.getMapEntry().getConfiguration().getTeams().get(teamKey)
				: null;

		if (team == null || team.getGenerator() == null) {
			Logger.warn("Cannot start emerald generator for team {}: team or generator location not found.", teamKey.getName());
			return;
		}

		HypixelPosition genLocation = team.getGenerator();
		Pos spawnPosition = new Pos(genLocation.x(), genLocation.y(), genLocation.z());

		// Define emerald generator properties (1 emerald every 60 seconds)
		final int emeraldDelaySeconds = 60;
		final int emeraldBaseAmount = 1;

		var emeraldTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
			if (game.getState() != GameState.IN_PROGRESS) return;

			int currentForgeLevel = game.getTeamUpgradeLevel(teamKey, TeamUpgradeId.FORGE);
			double multiplier = 1.0;
			if (currentForgeLevel >= 4) {
				multiplier = 3.0; // +200%
			}

			int finalAmount = (int) Math.round(emeraldBaseAmount * multiplier);
			if (finalAmount > 0) {
				ItemStack itemToSpawn = ItemStack.of(Material.EMERALD, finalAmount);
				ItemEntity itemEntity = new ItemEntity(itemToSpawn);
				itemEntity.setPickupDelay(Duration.ofMillis(500));
				itemEntity.setInstance(game.getInstance(), spawnPosition);
			}
		}).delay(TaskSchedule.seconds(emeraldDelaySeconds)).repeat(TaskSchedule.seconds(emeraldDelaySeconds)).schedule();

		game.getGeneratorManager().addTeamGeneratorTask(teamKey, emeraldTask);
	}
}
