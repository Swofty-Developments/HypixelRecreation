package net.swofty.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.text.Component;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.game.v2.BedWarsTeam;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayManager;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayMessages;
import net.swofty.type.bedwarsgame.stats.BedWarsStatsRecorder;
import net.swofty.type.game.game.event.TeamEliminatedEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

import java.util.Optional;

public class TeamEliminationListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void run(TeamEliminatedEvent<BedWarsTeam> event) {
        BedWarsTeam team = event.team();
        String teamColor = team.getColorCode();
        String teamName = team.getName();

        BedWarsGame game = (BedWarsGame) event.game();
        team.getPlayerIds().stream()
            .map(game::getPlayer)
            .flatMap(Optional::stream)
            .forEach(player -> {
                BedWarsStatsRecorder.recordBedLost(player, game.getGameType());
                BedWarsStatsRecorder.recordLoss(player, game.getGameType());
            });

        game.broadcastMessage(Component.newline().append(BedWarsReplayMessages.teamEliminated(team.getTeamKey())).appendNewline());

        BedWarsReplayManager replayManager = game.getReplayManager();

        // Record to replay
        if (replayManager.isRecording()) {
            replayManager.recordTeamElimination(team.getTeamKey());
        }
    }
}
