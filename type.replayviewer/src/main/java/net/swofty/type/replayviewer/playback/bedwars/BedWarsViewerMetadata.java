package net.swofty.type.replayviewer.playback.bedwars;

import net.swofty.type.game.replay.api.ReplayGameMetadata;

import java.util.List;
import java.util.UUID;

public record BedWarsViewerMetadata(String modeId, List<Team> teams,
                                    List<Generator> generators) implements ReplayGameMetadata {
    public BedWarsViewerMetadata {
        teams = List.copyOf(teams);
        generators = List.copyOf(generators);
    }

    public record Team(String id, String name, int color, List<UUID> initialMembers, Position bedFeet,
                       Position bedHead) {
        public Team {
            initialMembers = List.copyOf(initialMembers);
        }
    }

    public record Position(int x, int y, int z) {
    }

    public record Generator(String type, Position position) {
    }
}
