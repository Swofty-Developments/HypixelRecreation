package net.swofty.type.replayviewer.playback;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.type.game.replay.api.ReplayGameState;
import net.swofty.type.game.replay.api.ReplayViewerAdapter;
import net.swofty.type.game.replay.delta.ReplayBlockDelta;
import net.swofty.type.game.replay.delta.ReplayEntityRemoveDelta;
import net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta;
import net.swofty.type.game.replay.model.ReplaySnapshot;

import java.io.IOException;

public final class ReplayStateRestorer {
    private final ReplaySession session;
    private final ReplayTimeline timeline;
    private final ReplayWorldState world;
    private final ReplayEntityStore entities;
    private final ReplayViewerAdapter adapter;

    public ReplayStateRestorer(
            ReplaySession session,
            ReplayTimeline timeline,
            ReplayWorldState world,
            ReplayEntityStore entities,
            ReplayViewerAdapter adapter
    ) {
        this.session = session;
        this.timeline = timeline;
        this.world = world;
        this.entities = entities;
        this.adapter = adapter;
    }

    public void restore(int targetTick) {
        ReplaySnapshot snapshot = timeline.snapshotAtOrBefore(targetTick);
        session.beginStateRebuild();
        try {
            session.clearReplayOwnedState();
            world.restore(snapshot.blockOverlay());
            entities.restore(snapshot.entities());
            try (ReplayDataReader reader = new ReplayDataReader(snapshot.gameStatePayload())) {
                ReplayGameState gameState = (ReplayGameState) adapter.readState(reader);
                if (reader.available() != 0) throw new IOException("Trailing game snapshot state");
                adapter.restoreState(session, gameState);
            }
            for (var delta : timeline.stateDeltasBetween(snapshot.tick(), targetTick)) {
                if (delta instanceof ReplayBlockDelta block) world.apply(block.position(), block.blockStateId());
                else if (delta instanceof ReplayEntityUpsertDelta upsert) entities.upsert(upsert.entity());
                else if (delta instanceof ReplayEntityRemoveDelta remove) entities.remove(remove.replayEntityId());
                else adapter.applyDelta(session, delta);
            }
            session.rebuildEntityPresentation();
        } catch (Exception exception) {
            session.failPlayback(targetTick, exception);
            throw new IllegalStateException("Failed to restore replay state at tick " + targetTick, exception);
        } finally {
            session.endStateRebuild();
        }
    }
}
