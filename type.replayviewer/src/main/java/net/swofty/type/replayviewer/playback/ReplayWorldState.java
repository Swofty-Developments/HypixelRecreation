package net.swofty.type.replayviewer.playback;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.swofty.type.game.replay.model.ReplayBlockPosition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ReplayWorldState {
    private final InstanceContainer instance;
    private final Map<ReplayBlockPosition, Integer> baseline;
    private Map<ReplayBlockPosition, Integer> overlay = Map.of();

    public ReplayWorldState(InstanceContainer instance, Set<ReplayBlockPosition> replayPositions) {
        this.instance = instance;
        Map<ReplayBlockPosition, Integer> captured = new LinkedHashMap<>();
        for (ReplayBlockPosition position : replayPositions) {
            captured.put(position, instance.getBlock(position.x(), position.y(), position.z()).stateId());
        }
        baseline = Map.copyOf(captured);
    }

    public void restore(Map<ReplayBlockPosition, Integer> completeOverlay) {
        for (var entry : baseline.entrySet()) setBlock(entry.getKey(), entry.getValue());
        for (var entry : completeOverlay.entrySet()) setBlock(entry.getKey(), entry.getValue());
        overlay = Map.copyOf(completeOverlay);
    }

    public void apply(ReplayBlockPosition position, int stateId) {
        Map<ReplayBlockPosition, Integer> updated = new LinkedHashMap<>(overlay);
        updated.put(position, stateId);
        overlay = Map.copyOf(updated);
        setBlock(position, stateId);
    }

    public Map<ReplayBlockPosition, Integer> overlay() {
        return overlay;
    }

    private void setBlock(ReplayBlockPosition position, int stateId) {
        Block block = Block.fromStateId(stateId);
        if (block == null) throw new IllegalArgumentException("Unknown block state: " + stateId);
        instance.setBlock(position.x(), position.y(), position.z(), block);
    }
}
