package net.swofty.type.game.replay.api;

import net.swofty.type.game.replay.delta.ReplayBlockDelta;
import net.swofty.type.game.replay.delta.ReplayEntityRemoveDelta;
import net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta;
import net.swofty.type.game.replay.delta.ReplayGameStateDelta;
import net.swofty.type.game.replay.event.*;

public final class CoreReplayTypes {
    private CoreReplayTypes() {
    }

    public static ReplayTypeRegistry<ReplayStateDelta> deltas() {
        ReplayTypeRegistry<ReplayStateDelta> registry = new ReplayTypeRegistry<>();
        registry.register(ReplayBlockDelta.TYPE_ID, ReplayBlockDelta::read);
        registry.register(ReplayEntityUpsertDelta.TYPE_ID, ReplayEntityUpsertDelta::read);
        registry.register(ReplayEntityRemoveDelta.TYPE_ID, ReplayEntityRemoveDelta::read);
        registry.register(ReplayGameStateDelta.TYPE_ID, ReplayGameStateDelta::read);
        return registry;
    }

    public static ReplayTypeRegistry<ReplayEvent> events() {
        ReplayTypeRegistry<ReplayEvent> registry = new ReplayTypeRegistry<>();
        registry.register(ReplayComponentEvent.TYPE_ID, ReplayComponentEvent::read);
        registry.register(ReplayBookmarkEvent.TYPE_ID, ReplayBookmarkEvent::read);
        registry.register(ReplayEntityAnimationEvent.TYPE_ID, ReplayEntityAnimationEvent::read);
        registry.register(ReplayParticleEvent.TYPE_ID, ReplayParticleEvent::read);
        registry.register(ReplaySoundEvent.TYPE_ID, ReplaySoundEvent::read);
        registry.register(ReplayBlockBreakEvent.TYPE_ID, ReplayBlockBreakEvent::read);
        return registry;
    }
}
