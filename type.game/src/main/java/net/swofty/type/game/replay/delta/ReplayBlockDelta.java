package net.swofty.type.game.replay.delta;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayStateDelta;
import net.swofty.type.game.replay.model.ReplayBlockPosition;

import java.io.IOException;

public record ReplayBlockDelta(ReplayBlockPosition position, int blockStateId) implements ReplayStateDelta {
    public static final int TYPE_ID = 1000;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeBlockCoords(position.x(), position.y(), position.z());
        writer.writeVarInt(blockStateId);
    }

    public static ReplayBlockDelta read(ReplayDataReader reader) throws IOException {
        int[] position = reader.readBlockCoords();
        return new ReplayBlockDelta(new ReplayBlockPosition(position[0], position[1], position[2]), reader.readVarInt());
    }
}
