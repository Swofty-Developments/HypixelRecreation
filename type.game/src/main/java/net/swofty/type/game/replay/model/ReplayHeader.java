package net.swofty.type.game.replay.model;

import java.util.List;

public record ReplayHeader(ReplayMetadata metadata, List<Integer> snapshotIndex) {
    public ReplayHeader {
        snapshotIndex = List.copyOf(snapshotIndex);
    }
}
