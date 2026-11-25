package net.swofty.type.game.replay.delta;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayStateDelta;

import java.io.IOException;

public record ReplayGameStateDelta(int gameTypeId, byte[] payload) implements ReplayStateDelta {
    public static final int TYPE_ID = 1003;

    public ReplayGameStateDelta {
        payload = payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeVarInt(gameTypeId);
        writer.writeBytes(payload);
    }

    public static ReplayGameStateDelta read(ReplayDataReader reader) throws IOException {
        return new ReplayGameStateDelta(reader.readVarInt(), reader.readBytes());
    }
}
