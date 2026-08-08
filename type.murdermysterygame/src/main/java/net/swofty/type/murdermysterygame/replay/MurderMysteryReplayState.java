package net.swofty.type.murdermysterygame.replay;

import net.swofty.type.game.replay.api.ReplayGameState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MurderMysteryReplayState(
        Map<UUID, String> roles,
        Map<UUID, Integer> kills,
        List<UUID> eliminatedPlayers,
        boolean murdererReceivedSword,
        boolean detectiveBowAvailable,
        String winCondition,
        UUID winner
) implements ReplayGameState {
    public MurderMysteryReplayState {
        roles = Map.copyOf(roles);
        kills = Map.copyOf(kills);
        eliminatedPlayers = List.copyOf(eliminatedPlayers);
    }
}
