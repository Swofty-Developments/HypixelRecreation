package net.swofty.type.game.replay.dispatcher;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.type.game.replay.ReplayRecorder;

public final class EntityLocationDispatcher implements ReplayDispatcher {
    private ReplayRecorder recorder;
    private final Instance instance;

    public EntityLocationDispatcher(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(ReplayRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void tick() {
        for (Entity entity : instance.getEntities()) {
            if (recorder.isEntityReplayVisible(entity)) recorder.recordEntityState(entity);
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public String getName() {
        return "EntityState";
    }
}
