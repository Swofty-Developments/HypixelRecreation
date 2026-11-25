package net.swofty.type.game.replay.delta;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayStateDelta;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplaySnapshot;

import java.io.IOException;
import java.util.Map;

public record ReplayEntityUpsertDelta(ReplayEntityState entity) implements ReplayStateDelta {
    public static final int TYPE_ID = 1001;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeBytes(ReplaySnapshotCodec.write(new ReplaySnapshot(0, Map.of(), Map.of(entity.replayEntityId(), entity), new byte[0])));
    }

    public static ReplayEntityUpsertDelta read(ReplayDataReader reader) throws IOException {
        ReplaySnapshot snapshot = ReplaySnapshotCodec.read(reader.readBytes());
        if (snapshot.entities().size() != 1) throw new IOException("Entity upsert must contain exactly one entity");
        return new ReplayEntityUpsertDelta(snapshot.entities().values().iterator().next());
    }
}
