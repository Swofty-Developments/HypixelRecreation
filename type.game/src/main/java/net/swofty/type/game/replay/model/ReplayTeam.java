package net.swofty.type.game.replay.model;

import java.util.List;
import java.util.UUID;

public record ReplayTeam(String id, String name, int color, List<UUID> initialMembers) {
    public ReplayTeam {
        initialMembers = List.copyOf(initialMembers);
    }
}
