package net.swofty.type.bedwarsgame.shop.upgrades;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
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
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;

import java.util.List;

public class HealPoolUpgrade extends TeamUpgrade {

	public HealPoolUpgrade() {
		super(
			TeamUpgradeId.HEAL_POOL,
				"Heal Pool",
				"Your team permanently gains a heal pool at your base.",
				ItemStack.of(Material.BEACON),
				List.of(
						new TeamUpgradeTier(1, "Healing I", 1, Currency.DIAMOND)
				)
		);
	}

	@Override
	public void applyEffect(BedWarsGame game, TeamKey teamKey, int level) {
		MapTeam team = teamKey != null
				? game.getMapEntry().getConfiguration().getTeams().get(teamKey)
				: null;

		if (team == null) {
			return;
		}

		HypixelPosition spawnPos = team.getSpawn();
		Pos teamSpawn = new Pos(spawnPos.x(), spawnPos.y(), spawnPos.z(), spawnPos.pitch(), spawnPos.yaw());

		MinecraftServer.getSchedulerManager().buildTask(() -> {
			if (game.getState() != GameState.IN_PROGRESS) {
				return;
			}

			for (BedWarsPlayer player : game.getPlayers()) {
				if (teamKey.equals(player.getTeamKey())) {
					if (player.getPosition().distance(teamSpawn) <= 15) {
						// Heal player by 1 heart (2 health)
						double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
						if (player.getHealth() < maxHealth) {
							player.setHealth(Math.min(player.getHealth() + 2.0f, (float) maxHealth));
						}
					}
				}
			}
		}).repeat(TaskSchedule.seconds(7)).schedule();
	}

}
