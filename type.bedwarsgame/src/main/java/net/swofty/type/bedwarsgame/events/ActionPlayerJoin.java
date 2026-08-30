package net.swofty.type.bedwarsgame.events;

import lombok.SneakyThrows;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.commons.ServerType;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.Game.JoinResult;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.redis.service.GameInformationHandler;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

public class ActionPlayerJoin implements HypixelEventClass {

	@SneakyThrows
	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(AsyncPlayerConfigurationEvent event) {
		final BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		Logger.info("Player " + player.getUsername() + " joined the server from start server " + player.getOriginServer());
		event.setSpawningInstance(HypixelConst.getEmptyInstance());
		player.setRespawnPoint(new Pos(10, 10, 10));

		ScheduleUtility.delay(() -> tryJoinGame(player, false), 15);
	}

	private void tryJoinGame(BedWarsPlayer player, boolean isRetry) {
		if (!player.isOnline()) return;

		String preferredGameId = GameInformationHandler.game.remove(player.getUuid());
		if (preferredGameId == null) {
			if (!isRetry) {
				Logger.info("No game assignment found for " + player.getUsername() + ", retrying in 1 second...");
				ScheduleUtility.delay(() -> tryJoinGame(player, true), 20);
				return;
			}
			Logger.error("Failed to find game assignment for player " + player.getUsername());
			player.sendMessage("<c>No game assignment found! Returning to lobby...");
			player.sendTo(ServerType.BEDWARS_LOBBY);
			return;
		}

		BedWarsGame preferred = TypeBedWarsGameLoader.getGameById(preferredGameId);
		if (preferred == null) {
			player.sendMessage("<c>The assigned game no longer exists! Returning to lobby...");
			player.sendTo(ServerType.BEDWARS_LOBBY);
			return;
		}

		ScheduleUtility.delay(() -> {
			if (!player.isOnline()) return;
			if (preferred.hasDisconnectedPlayer(player.getUuid())) {
				if (!preferred.handleRejoin(player)) {
					player.sendMessage("<c>Failed to rejoin your previous game! Returning to lobby...");
					player.sendTo(ServerType.BEDWARS_LOBBY);
				}
				return;
			}

			if (preferred.getState() != GameState.WAITING && preferred.getState() != GameState.COUNTDOWN) {
				player.sendMessage("<c>The assigned game is no longer joinable! Returning to lobby...");
				player.sendTo(ServerType.BEDWARS_LOBBY);
				return;
			}

			JoinResult joinResult = preferred.join(player);
			if (joinResult instanceof JoinResult.Denied(String reason)) {
				player.sendMessage("<c>Failed to join assigned game ({})! Returning to lobby...", reason);
				player.sendTo(ServerType.BEDWARS_LOBBY);
			}
		}, 15);
	}
}

