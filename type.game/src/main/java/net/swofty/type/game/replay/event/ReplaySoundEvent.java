package net.swofty.type.game.replay.event;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.ReplayEvent;

import java.io.IOException;

public record ReplaySoundEvent(String soundId, byte source, double x, double y, double z, float volume, float pitch)
        implements ReplayEvent {
    public static final int TYPE_ID = 1004;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    public void write(ReplayDataWriter writer) throws IOException {
        writer.writeString(soundId);
        writer.writeByte(source);
        writer.writeDouble(x);
        writer.writeDouble(y);
        writer.writeDouble(z);
        writer.writeFloat(volume);
        writer.writeFloat(pitch);
    }

    public static ReplaySoundEvent read(ReplayDataReader reader) throws IOException {
        return new ReplaySoundEvent(reader.readString(), (byte) reader.readByte(), reader.readDouble(), reader.readDouble(),
                reader.readDouble(), reader.readFloat(), reader.readFloat());
    }
}
