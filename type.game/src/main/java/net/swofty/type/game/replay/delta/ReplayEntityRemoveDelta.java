package net.swofty.type.game.replay.delta;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayStateDelta;

import java.io.IOException;

public record ReplayEntityRemoveDelta(int replayEntityId) implements ReplayStateDelta {
    public static final int TYPE_ID = 1002;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeVarInt(replayEntityId);
    }

    public static ReplayEntityRemoveDelta read(ReplayDataReader reader) throws IOException {
        return new ReplayEntityRemoveDelta(reader.readVarInt());
    }
}
