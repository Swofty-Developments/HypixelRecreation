package net.swofty.type.game.replay.dispatcher;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta;
import net.swofty.type.game.replay.model.ReplayEntityState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EntityLocationDispatcher implements ReplayDispatcher {
    private ReplayRecorder recorder;
    private final Instance instance;
    private final Map<Integer, ReplayEntityState> lastStates = new HashMap<>();

    public EntityLocationDispatcher(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(ReplayRecorder recorder) {
        this.recorder = recorder;
        for (Entity entity : instance.getEntities()) {
            ReplayEntityState state = recorder.captureEntityState(entity);
            if (state != null) lastStates.put(entity.getEntityId(), state);
        }
    }

    @Override
    public void tick() {
        Set<Integer> visibleEntities = new HashSet<>();
        for (Entity entity : instance.getEntities()) {
            ReplayEntityState state = recorder.captureEntityState(entity);
            if (state == null) continue;

            int entityId = entity.getEntityId();
            visibleEntities.add(entityId);
            ReplayEntityState previous = lastStates.get(entityId);
            if (!state.equals(previous)) {
                recorder.recordDelta(new ReplayEntityUpsertDelta(state));
            }
            lastStates.put(entityId, state);
        }
        lastStates.keySet().removeIf(entityId -> !visibleEntities.contains(entityId));
    }

    @Override
    public void cleanup() {
        lastStates.clear();
    }

    @Override
    public String getName() {
        return "EntityState";
    }
}
