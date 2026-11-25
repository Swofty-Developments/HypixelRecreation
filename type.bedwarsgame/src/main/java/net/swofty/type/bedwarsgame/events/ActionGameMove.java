package net.swofty.type.bedwarsgame.events;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.item.impl.LuckyBlockTrap;
import net.swofty.type.bedwarsgame.item.impl.LuckyEquipmentEffects;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

import java.util.List;
import java.util.Map;

public class ActionGameMove implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true)
	public void run(PlayerMoveEvent event) {
		BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();

		if (!player.isOnline()) {
			return;
		}

		BedWarsGame game = player.getGame();
		if (game == null || game.getState() != GameState.IN_PROGRESS) {
			return;
		}

		if (player.getPosition().y() <= 0) {
			ActionGameDeath.death(player, game, game::broadcastMessage, true);
			return;
		}

		TeamKey playerTeamKey = player.getTeamKey();
		if (playerTeamKey == null) {
			return;
		}

		LuckyEquipmentEffects.tick(player);

		Point playerPos = player.getPosition();
		Point trapPos = new Pos(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ());
		if (LuckyBlockTrap.trigger(player, trapPos, player.getInstance().getBlock(trapPos))) {
			return;
		}
		Point belowTrapPos = new Pos(playerPos.blockX(), playerPos.blockY() - 1, playerPos.blockZ());
		if (LuckyBlockTrap.trigger(player, belowTrapPos, player.getInstance().getBlock(belowTrapPos))) {
			return;
		}

		// check what team this is
		for (Map.Entry<TeamKey, MapTeam> entry : game.getMapEntry().getConfiguration().getTeams().entrySet()) {
			TeamKey teamKey = entry.getKey();
			MapTeam team = entry.getValue();

			// Don't trigger own team's traps
			if (teamKey.equals(playerTeamKey)) {
				continue;
			}

			BedWarsMapsConfig.TwoBlockPosition bedPos = team.getBed();
			if (bedPos == null || bedPos.feet() == null) {
				continue;
			}

			Point bedLocation = new Pos(bedPos.feet().x(), bedPos.feet().y(), bedPos.feet().z());
			if (playerPos.distance(bedLocation) <= 10) {
				List<TrapId> teamTraps = game.getTeamTraps(teamKey);
				if (teamTraps.isEmpty()) {
					continue;
				}

				var trap = TypeBedWarsGameLoader.getTrapManager().getTrap(teamTraps.getFirst());
				if (trap != null) {
					trap.trigger(game, teamKey, player);
				}
			}
		}
	}
}
