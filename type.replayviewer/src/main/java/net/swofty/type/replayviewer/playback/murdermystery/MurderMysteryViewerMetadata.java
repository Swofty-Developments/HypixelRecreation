package net.swofty.type.replayviewer.playback.murdermystery;

import net.swofty.type.game.replay.api.ReplayGameMetadata;

import java.util.List;
import java.util.UUID;

public record MurderMysteryViewerMetadata(String modeId, List<Player> players) implements ReplayGameMetadata {
    public MurderMysteryViewerMetadata {
        players = List.copyOf(players);
    }

    public record Player(UUID uuid, String role, UUID target) {
    }
}
