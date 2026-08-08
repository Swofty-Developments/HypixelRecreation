package net.swofty.type.replayviewer.playback.murdermystery;

import net.swofty.type.game.replay.api.ReplayGameState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MurderMysteryViewerState(
        Map<UUID, String> roles,
        Map<UUID, Integer> kills,
        List<UUID> eliminatedPlayers,
        boolean murdererReceivedSword,
        boolean detectiveBowAvailable,
        String winCondition,
        UUID winner
) implements ReplayGameState {
    public MurderMysteryViewerState {
        roles = Map.copyOf(roles);
        kills = Map.copyOf(kills);
        eliminatedPlayers = List.copyOf(eliminatedPlayers);
    }
}
