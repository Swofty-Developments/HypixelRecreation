package net.swofty.type.game.replay.model;

import java.util.Map;

public record ReplaySnapshot(
        int tick,
        Map<ReplayBlockPosition, Integer> blockOverlay,
        Map<Integer, ReplayEntityState> entities,
        byte[] gameStatePayload
) {
    public ReplaySnapshot {
        if (tick < 0) throw new IllegalArgumentException("tick must not be negative");
        blockOverlay = Map.copyOf(blockOverlay);
        entities = Map.copyOf(entities);
        gameStatePayload = gameStatePayload.clone();
    }

    @Override
    public byte[] gameStatePayload() {
        return gameStatePayload.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ReplaySnapshot snapshot
                && tick == snapshot.tick
                && blockOverlay.equals(snapshot.blockOverlay)
                && entities.equals(snapshot.entities)
                && java.util.Arrays.equals(gameStatePayload, snapshot.gameStatePayload);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Objects.hash(tick, blockOverlay, entities) + java.util.Arrays.hashCode(gameStatePayload);
    }
}
