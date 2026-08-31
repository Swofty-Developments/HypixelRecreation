package net.swofty.type.game.replay.api;

import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplayTypeRegistry<T> {
    private final Map<Integer, ReplayEntryReader<? extends T>> readers = new ConcurrentHashMap<>();

    public void register(int typeId, ReplayEntryReader<? extends T> reader) {
        if (typeId < 0) throw new IllegalArgumentException("typeId must not be negative");
        if (readers.putIfAbsent(typeId, reader) != null) {
            throw new IllegalStateException("Replay type already registered: " + typeId);
        }
    }

    public T read(int typeId, byte[] payload) throws IOException {
        ReplayEntryReader<? extends T> reader = readers.get(typeId);
        if (reader == null) throw new IOException("Unknown required replay entry type: " + typeId);
        try (ReplayDataReader data = new ReplayDataReader(payload)) {
            T value = reader.read(data);
            if (data.available() != 0) throw new IOException("Trailing data for replay entry type " + typeId);
            return value;
        }
    }

    public static byte[] encode(int tick, int typeId, ReplayEntryWriter entry) throws IOException {
        ReplayDataWriter payload = new ReplayDataWriter();
        entry.write(payload);
        ReplayDataWriter writer = new ReplayDataWriter();
        writer.writeVarInt(tick);
        writer.writeVarInt(typeId);
        writer.writeBytes(payload.toByteArray());
        return writer.toByteArray();
    }

    public DecodedEntry<T> decode(byte[] encoded) throws IOException {
        try (ReplayDataReader reader = new ReplayDataReader(encoded)) {
            int tick = reader.readVarInt();
            int typeId = reader.readVarInt();
            byte[] payload = reader.readBytes();
            if (reader.available() != 0) throw new IOException("Trailing replay entry framing data");
            return new DecodedEntry<>(tick, typeId, read(typeId, payload));
        }
    }

    @FunctionalInterface
    public interface ReplayEntryWriter {
        void write(ReplayDataWriter writer) throws IOException;
    }

    public record DecodedEntry<T>(int tick, int typeId, T value) {
        public DecodedEntry {
            if (tick < 0) throw new IllegalArgumentException("tick must not be negative");
        }
    }
}
