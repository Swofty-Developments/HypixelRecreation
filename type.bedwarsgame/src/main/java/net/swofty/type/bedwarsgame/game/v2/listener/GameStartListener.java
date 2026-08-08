package net.swofty.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.mc.Vec3i;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.event.GameStartEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.i18n.I18n;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.Map;

public class GameStartListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onGameStart(GameStartEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        Logger.info("Starting BedWars game {}", game.getGameId());

        // Prepare world
        game.getWorldManager().clearExistingBeds();
        removeWaitingLobby(game);

        // Assign players to teams
        game.autoAssignTeams();
        game.getActiveTeams().forEach(team -> team.setBedAlive(true));

        // Get active teams and set up their areas
        Map<BedWarsMapsConfig.TeamKey, BedWarsMapsConfig.MapTeam> activeTeamConfigs = game.getActiveTeamConfigs();

        game.getWorldManager().placeBeds(activeTeamConfigs);
        if (!game.getGameType().isOneBlock()) {
            game.getWorldManager().spawnShopNPCs(activeTeamConfigs);
        }

        // Start generators
        if (!game.getGameType().isOneBlock()) {
            game.getGeneratorManager().startTeamGenerators(activeTeamConfigs);
            game.getGeneratorManager().startGlobalGenerators();
        }

        // Start game event progression
        game.getGameEventManager().start();
        game.getOneBlockManager().start();

        // Teleport players to their spawn points
        game.teleportPlayersToSpawns();

        // Start time-played XP task
        game.startTimePlayedRewards();

        // Start replay recording
        game.getReplayManager().startRecording();

        // Send game start message
        game.sendGameStartMessage();
        game.getSwappageManager().start();

        // just correct in case
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (game.getState() != GameState.IN_PROGRESS) {
                return TaskSchedule.stop();
            }
            for (BedWarsPlayer player : game.getPlayers()) {
                player.updateBelowTag();
            }
            return TaskSchedule.seconds(10);
        }, TaskSchedule.seconds(1));

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (game.getState() != GameState.IN_PROGRESS) {
                return TaskSchedule.stop();
            }
            game.getTrackers().forEach((uuid, teamKey) -> {
                game.getPlayer(uuid).ifPresentOrElse(player -> {
                    // get closest player from teamKey
                    BedWarsPlayer target = game.getPlayersOnTeam(teamKey)
                        .stream()
                        .filter(t -> !t.getUuid().equals(uuid))
                        .min(Comparator.comparingDouble(t ->
                            player.getPosition().distanceSquared(t.getPosition())
                        ))
                        .orElse(null);

                    // team could be completely eliminated
                    if (target == null) {
                        game.getTrackers().remove(uuid);
                        return;
                    }

                    int distance = (int) Math.sqrt(
                        player.getPosition().distanceSquared(target.getPosition())
                    );

                    Component playerColouredName = target.getDisplayName();
                    if (playerColouredName == null) {
                        playerColouredName = Component.text(target.getUsername());
                    }

                    player.sendActionBar(
                        I18n.t(
                            "bedwars.tracking_player",
                            Argument.component("name", playerColouredName),
                            Argument.string("distance", String.valueOf(distance))
                        )
                    );
                }, () -> game.getTrackers().remove(uuid));
            });
            return TaskSchedule.seconds(2);
        }, TaskSchedule.seconds(15));

        Logger.info("BedWars game {} started with {} active teams", event.game().getGameId(), activeTeamConfigs.size());
    }

    private void removeWaitingLobby(BedWarsGame game) {
        var selection = game.getMapEntry().getConfiguration().getLocations().getWaitingLobby();
        if (selection == null || selection.min() == null || selection.max() == null) return;
        Vec3i min = selection.min();
        Vec3i max = selection.max();
        for (int x = Math.min(min.x(), max.x()); x <= Math.max(min.x(), max.x()); x++) {
            for (int y = Math.min(min.y(), max.y()); y <= Math.max(min.y(), max.y()); y++) {
                for (int z = Math.min(min.z(), max.z()); z <= Math.max(min.z(), max.z()); z++) {
                    game.getInstance().setBlock(x, y, z, Block.AIR);
                }
            }
        }
    }

}
