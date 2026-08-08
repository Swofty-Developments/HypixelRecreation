package net.swofty.type.game.replay.event;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;
import net.swofty.type.game.replay.model.ReplayBlockPosition;

import java.io.IOException;

public record ReplayBlockBreakEvent(int entityId, ReplayBlockPosition position, byte stage) implements ReplayEvent {
    public static final int TYPE_ID = 1005;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeVarInt(entityId);
        writer.writeBlockCoords(position.x(), position.y(), position.z());
        writer.writeByte(stage);
    }

    public static ReplayBlockBreakEvent read(ReplayDataReader reader) throws IOException {
        int entityId = reader.readVarInt();
        int[] position = reader.readBlockCoords();
        return new ReplayBlockBreakEvent(entityId, new ReplayBlockPosition(position[0], position[1], position[2]),
                (byte) reader.readByte());
    }
}
