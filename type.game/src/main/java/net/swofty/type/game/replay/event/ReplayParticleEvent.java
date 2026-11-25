package net.swofty.type.game.replay.event;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;

import java.io.IOException;

public record ReplayParticleEvent(byte[] packet) implements ReplayEvent {
    public static final int TYPE_ID = 1003;

    public ReplayParticleEvent {
        packet = packet.clone();
    }

    @Override
    public byte[] packet() {
        return packet.clone();
    }

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeBytes(packet);
    }

    public static ReplayParticleEvent read(ReplayDataReader reader) throws IOException {
        return new ReplayParticleEvent(reader.readBytes());
    }
}
