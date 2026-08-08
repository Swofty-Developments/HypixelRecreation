package net.swofty.type.bedwarsgame.replay;

import net.swofty.type.game.replay.api.ReplayGameMetadata;

import java.util.List;
import java.util.UUID;

public record BedWarsReplayMetadata(String modeId, List<TeamDefinition> teams,
                                    List<GeneratorDefinition> generators) implements ReplayGameMetadata {
    public BedWarsReplayMetadata {
        teams = List.copyOf(teams);
        generators = List.copyOf(generators);
    }

    public record TeamDefinition(
            String id,
            String name,
            int color,
            List<UUID> initialMembers,
            BlockPosition bedFeet,
            BlockPosition bedHead
    ) {
        public TeamDefinition {
            initialMembers = List.copyOf(initialMembers);
        }
    }

    public record BlockPosition(int x, int y, int z) {
    }

    public record GeneratorDefinition(String type, BlockPosition position) {
    }
}
