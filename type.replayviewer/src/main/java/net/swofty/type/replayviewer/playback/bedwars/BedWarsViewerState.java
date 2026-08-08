package net.swofty.type.replayviewer.playback.bedwars;

import net.swofty.type.game.replay.api.ReplayGameState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BedWarsViewerState(
        Map<String, List<UUID>> teamMembers,
        Map<String, Boolean> liveBeds,
        Map<String, Integer> generatorTiers,
        List<String> scoreboardJson,
        List<String> eliminatedTeams,
        String winnerId,
        List<DisplayState> displays,
        List<NpcState> npcs
) implements ReplayGameState {
    public BedWarsViewerState {
        teamMembers = teamMembers.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
        liveBeds = Map.copyOf(liveBeds);
        generatorTiers = Map.copyOf(generatorTiers);
        scoreboardJson = List.copyOf(scoreboardJson);
        eliminatedTeams = List.copyOf(eliminatedTeams);
        displays = List.copyOf(displays);
        npcs = List.copyOf(npcs);
    }

    public record DisplayState(int entityId, UUID uuid, double x, double y, double z,
                               List<String> lines, String displayType, String identifier) {
        public DisplayState {
            lines = List.copyOf(lines);
        }
    }

    public record NpcState(int entityId, String displayName, List<String> lines) {
        public NpcState {
            lines = List.copyOf(lines);
        }
    }
}
