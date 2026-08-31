package net.swofty.type.bedwarsgame.events;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.util.HeldItems;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.replay.dispatcher.BlockChangeDispatcher;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import org.tinylog.Logger;

public class ActionGamePlace implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerBlockPlaceEvent event) {
		BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		if (event.isCancelled()) return;
		BedWarsGame game = player.getGame();
		if (game == null) {
			Logger.info("Player {} tried to place a block but is not in a game!", player.getUsername());
			event.setCancelled(true); // Prevent placing if not in a game
			return;
		}

		if (!game.isPlayerCurrentlyPlaying(player.getUuid())) {
			event.setCancelled(true);
			return;
		}

		if (event.getBlockPosition().y() >= 105) {
			player.sendMessage("<c>You cannot place blocks this high!");
			event.setCancelled(true);
			return;
		}

		if (game.getState() != GameState.IN_PROGRESS) {
			event.setCancelled(true);
			return;
		}

		Point blockPosition = event.getBlockPosition();
		for (MapTeam team : game.getMapEntry().getConfiguration().getTeams().values()) {
			HypixelPosition spawnPos = team.getSpawn();
			if (spawnPos != null) {
				Point spawnPoint = new Pos(spawnPos.x(), spawnPos.y(), spawnPos.z());
				if (blockPosition.distance(spawnPoint) <= 6) {
					player.sendMessage("<c>You cannot build here.");
					event.setCancelled(true);
					return;
				}
			}
		}

		if (event.getBlock().compare(Block.TNT)) {
			ExplosionSystem explosions = Polyp.getInstance().services().explosion();
			if (explosions == null) {
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			if (explosions.primeTnt(event.getInstance(), blockPosition, player, TntPrimeEvent.Cause.PLACEMENT) != null) {
				HeldItems.consumeOne(player, event.getHand());
			}
			return;
		}

		Block placedBlock = event.getBlock().withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true);
		if (game.getReplayManager().isRecording()) {
			BlockChangeDispatcher blockDispatcher = game.getReplayManager().getBlockChangeDispatcher();
			if (blockDispatcher != null) {
				Block previousBlock = event.getInstance().getBlock(blockPosition);
				blockDispatcher.recordBlockChange(
					blockPosition.blockX(),
					blockPosition.blockY(),
					blockPosition.blockZ(),
					previousBlock.stateId(),
					placedBlock.stateId()
				);
			}
		}
		event.setBlock(placedBlock);
        if (player.allowsPersistentProgress()) {
            player.getAchievementHandler().addProgressByTrigger("bedwars.blocks_placed", 1);
        }
	}

}
