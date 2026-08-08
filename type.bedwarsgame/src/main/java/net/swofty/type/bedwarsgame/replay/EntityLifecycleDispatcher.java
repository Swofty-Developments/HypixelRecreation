package net.swofty.type.bedwarsgame.replay;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.delta.ReplayEntityRemoveDelta;
import net.swofty.type.game.replay.dispatcher.ReplayDispatcher;
import net.swofty.type.game.replay.event.ReplayEntityAnimationEvent;

import java.util.HashSet;
import java.util.Set;

public final class EntityLifecycleDispatcher implements ReplayDispatcher {
    private final Instance instance;
    private final Set<Integer> trackedEntities = new HashSet<>();
    private ReplayRecorder recorder;

    public EntityLifecycleDispatcher(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void initialize(ReplayRecorder recorder) {
        this.recorder = recorder;
        for (Entity entity : instance.getEntities()) {
            if (recorder.isEntityReplayVisible(entity)) trackedEntities.add(entity.getEntityId());
        }
    }

    @Override
    public void tick() {
        Set<Integer> current = new HashSet<>();
        for (Entity entity : instance.getEntities()) {
            if (!recorder.isEntityReplayVisible(entity)) continue;
            current.add(entity.getEntityId());
            if (trackedEntities.add(entity.getEntityId())) recorder.recordEntityState(entity);
        }
        Set<Integer> removed = new HashSet<>(trackedEntities);
        removed.removeAll(current);
        for (int entityId : removed) {
            recorder.recordDelta(new ReplayEntityRemoveDelta(entityId));
            trackedEntities.remove(entityId);
        }
    }

    public void recordArmSwing(int entityId, boolean mainHand) {
        recorder.recordEvent(new ReplayEntityAnimationEvent(entityId, mainHand
                ? ReplayEntityAnimationEvent.Animation.SWING_MAIN_HAND
                : ReplayEntityAnimationEvent.Animation.SWING_OFF_HAND));
    }

    public void recordAnimation(int entityId, ReplayEntityAnimationEvent.Animation animationType) {
        recorder.recordEvent(new ReplayEntityAnimationEvent(entityId, animationType));
    }

    public void recordHeldItem(int entityId, ItemStack item) {
        recordCurrentState(entityId);
    }

    public void recordEquipment(int entityId, int slot, ItemStack itemStack) {
        recordCurrentState(entityId);
    }

    public void recordEntityEffect(int entityId, int effectId, byte amplifier, int durationTicks, byte flags) {
        recordCurrentState(entityId);
    }

    private void recordCurrentState(int entityId) {
        Entity entity = instance.getEntityById(entityId);
        if (entity != null) recorder.recordEntityState(entity);
    }

    @Override
    public void cleanup() {
        trackedEntities.clear();
    }

    @Override
    public String getName() {
        return "EntityLifecycle";
    }
}
