package net.swofty.type.murdermysterygame.replay;

import net.swofty.type.game.replay.api.ReplayGameMetadata;

import java.util.List;
import java.util.UUID;

public record MurderMysteryReplayMetadata(String modeId, List<PlayerDefinition> players) implements ReplayGameMetadata {
    public MurderMysteryReplayMetadata {
        players = List.copyOf(players);
    }

    public record PlayerDefinition(UUID uuid, String role, UUID target) {
    }
}
