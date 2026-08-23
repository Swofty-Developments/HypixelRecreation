package net.swofty.type.bedwarsgame.game.v2;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class SwappageManager {
    private final BedWarsGame game;
    private Task task;

    public void start() {
        if (!game.getGameType().isSwappage() || task != null) return;
        scheduleNext();
    }

    public void stop() {
        if (task != null) task.cancel();
        task = null;
    }

    private void scheduleNext() {
        int seconds = ThreadLocalRandom.current().nextInt(60, 241);
        task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            task = null;
            if (game.getState() != GameState.IN_PROGRESS) return;
            swapPartition(game.getViableTeams().stream().filter(BedWarsTeam::isBedAlive).toList());
            swapPartition(game.getViableTeams().stream().filter(team -> !team.isBedAlive()).toList());
            scheduleNext();
        }).delay(TaskSchedule.seconds(seconds)).schedule();
    }

    private void swapPartition(List<BedWarsTeam> source) {
        List<BedWarsTeam> teams = new ArrayList<>(source);
        Collections.shuffle(teams);
        for (int i = 0; i + 1 < teams.size(); i += 2) {
            swap(teams.get(i), teams.get(i + 1));
        }
    }

    private void swap(BedWarsTeam first, BedWarsTeam second) {
        List<UUID> firstPlayers = new ArrayList<>(first.getPlayerIds());
        List<UUID> secondPlayers = new ArrayList<>(second.getPlayerIds());
        List<Pos> firstPositions = activePositions(firstPlayers);
        List<Pos> secondPositions = activePositions(secondPlayers);

        game.swapTeamState(first, second);
        firstPlayers.forEach(uuid -> game.reassignPlayer(uuid, second));
        secondPlayers.forEach(uuid -> game.reassignPlayer(uuid, first));

        updatePlayers(firstPlayers, second, secondPositions);
        updatePlayers(secondPlayers, first, firstPositions);
    }

    private List<Pos> activePositions(List<UUID> players) {
        return players.stream()
            .map(game::getPlayer)
            .flatMap(Optional::stream)
            .filter(player -> game.isPlayerCurrentlyPlaying(player.getUuid()))
            .map(BedWarsPlayer::getPosition)
            .toList();
    }

    private void updatePlayers(List<UUID> playerIds, BedWarsTeam team, List<Pos> positions) {
        for (UUID uuid : playerIds) {
            game.getPlayer(uuid).ifPresent(player -> {
                player.setDisplayName(player.getColouredName());
                player.updateBelowTag();
                game.equipTeamArmor(player, team.getTeamKey());
                game.getReplayManager().recordPlayerTeam(player);

                if (!game.getRespawnHandler().isRespawning(uuid)
                    && game.isPlayerCurrentlyPlaying(uuid) && !positions.isEmpty()) {
                    player.teleport(positions.get(ThreadLocalRandom.current().nextInt(positions.size())));
                    player.playTeleportSound();
                }

                var teamName = Component.text(team.getName(), TextColor.color(team.getTeamKey().rgb()));
                player.showTitle(Text.key("bedwars.swap_title"), Text.key("bedwars.swap_subtitle", teamName));
                player.sendMessage(Text.key("bedwars.swap_team_changed", teamName));
            });
        }
    }

}
