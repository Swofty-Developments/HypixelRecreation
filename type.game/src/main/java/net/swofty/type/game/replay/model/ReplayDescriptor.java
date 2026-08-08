package net.swofty.type.game.replay.model;

import net.swofty.commons.ServerType;

import java.util.UUID;

public record ReplayDescriptor(
        UUID replayId,
        String gameId,
        String gameType,
        ServerType serverType,
        String serverId,
        String mapName,
        String mapHash,
        double mapCenterX,
        double mapCenterZ,
        int formatVersion,
        long startTime,
        long endTime,
        int durationTicks,
        long dataSize
) {
    public ReplayDescriptor {
        java.util.Objects.requireNonNull(replayId, "replayId");
        java.util.Objects.requireNonNull(serverType, "serverType");
        if (gameId == null || gameId.isBlank() || gameType == null || gameType.isBlank()) {
            throw new IllegalArgumentException("Replay game identity is required");
        }
        if (formatVersion < 1 || durationTicks < 0 || dataSize < 0 || endTime < 0 || startTime < 0) {
            throw new IllegalArgumentException("Invalid replay descriptor values");
        }
    }
}
