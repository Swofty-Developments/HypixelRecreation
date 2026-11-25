package net.swofty.commons.replay.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32C;

public final class ReplayFormat {
    public static final int MAGIC = 0x53575250;
    public static final int MAJOR_VERSION = 4;
    public static final int MAX_TICKS = 20 * 60 * 60 * 24;
    public static final int MAX_RECORDS_PER_CHUNK = 1_000_000;
    public static final int MAX_ENTRY_BYTES = 16 * 1024 * 1024;
    public static final int MAX_CHUNK_BYTES = 64 * 1024 * 1024;

    private ReplayFormat() {
    }

    public static ReplayChunk createChunk(
            ReplaySection section,
            int sequence,
            int startTick,
            int endTick,
            List<byte[]> entries
    ) throws IOException {
        if (entries.size() > MAX_RECORDS_PER_CHUNK) {
            throw new IOException("Replay chunk contains too many records: " + entries.size());
        }
        ReplayDataWriter writer = new ReplayDataWriter();
        writer.writeInt(MAGIC);
        writer.writeVarInt(MAJOR_VERSION);
        writer.writeByte(section.id());
        writer.writeVarInt(sequence);
        writer.writeVarInt(startTick);
        writer.writeVarInt(endTick);
        writer.writeVarInt(entries.size());
        for (byte[] entry : entries) {
            if (entry.length > MAX_ENTRY_BYTES) {
                throw new IOException("Replay entry is too large: " + entry.length);
            }
            writer.writeBytes(entry);
        }
        byte[] payload = writer.toByteArray();
        if (payload.length > MAX_CHUNK_BYTES) {
            throw new IOException("Replay chunk is too large: " + payload.length);
        }
        return new ReplayChunk(
                section,
                sequence,
                startTick,
                endTick,
                payload.length,
                entries.size(),
                checksum(payload),
                ReplayCompression.compress(payload)
        );
    }

    public static List<byte[]> readChunk(ReplayChunk chunk) throws IOException {
        validateHeader(chunk);
        byte[] payload = ReplayCompression.decompress(chunk.compressedPayload(), MAX_CHUNK_BYTES);
        if (payload.length != chunk.uncompressedLength()) {
            throw new IOException("Replay chunk length mismatch for sequence " + chunk.sequence());
        }
        if (checksum(payload) != chunk.checksum()) {
            throw new IOException("Replay chunk checksum mismatch for sequence " + chunk.sequence());
        }
        try (ReplayDataReader reader = new ReplayDataReader(payload)) {
            if (reader.readInt() != MAGIC) throw new IOException("Invalid replay magic");
            int version = reader.readVarInt();
            if (version != MAJOR_VERSION) throw new IOException("Unsupported replay format version: " + version);
            ReplaySection payloadSection;
            try {
                payloadSection = ReplaySection.fromId(reader.readUnsignedByte());
            } catch (IllegalArgumentException exception) {
                throw new IOException(exception.getMessage(), exception);
            }
            if (payloadSection != chunk.section()) throw new IOException("Replay section header mismatch");
            if (reader.readVarInt() != chunk.sequence()) throw new IOException("Replay sequence header mismatch");
            if (reader.readVarInt() != chunk.startTick() || reader.readVarInt() != chunk.endTick()) {
                throw new IOException("Replay tick range header mismatch");
            }
            int count = reader.readVarInt();
            if (count != chunk.recordCount() || count > MAX_RECORDS_PER_CHUNK) {
                throw new IOException("Replay record count mismatch");
            }
            List<byte[]> entries = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                entries.add(reader.readBytes(MAX_ENTRY_BYTES));
            }
            if (reader.available() != 0) throw new IOException("Trailing replay chunk data");
            return List.copyOf(entries);
        }
    }

    public static void validateOrdered(List<ReplayChunk> chunks, ReplaySection section) throws IOException {
        int expectedSequence = 0;
        int previousEndTick = -1;
        for (ReplayChunk chunk : chunks.stream().filter(value -> value.section() == section)
                .sorted(java.util.Comparator.comparingInt(ReplayChunk::sequence)).toList()) {
            if (chunk.sequence() != expectedSequence++) {
                throw new IOException("Missing " + section + " replay chunk sequence " + (expectedSequence - 1));
            }
            if (chunk.startTick() < previousEndTick) {
                throw new IOException("Overlapping " + section + " replay chunk at sequence " + chunk.sequence());
            }
            readChunk(chunk);
            previousEndTick = chunk.endTick();
        }
    }

    private static void validateHeader(ReplayChunk chunk) throws IOException {
        if (chunk.startTick() < 0 || chunk.endTick() < chunk.startTick() || chunk.endTick() > MAX_TICKS) {
            throw new IOException("Invalid replay tick range " + chunk.startTick() + "-" + chunk.endTick());
        }
        if (chunk.uncompressedLength() > MAX_CHUNK_BYTES || chunk.recordCount() > MAX_RECORDS_PER_CHUNK
                || chunk.compressedPayload().length > MAX_CHUNK_BYTES) {
            throw new IOException("Replay chunk exceeds configured limits");
        }
    }

    private static int checksum(byte[] payload) {
        CRC32C checksum = new CRC32C();
        checksum.update(payload, 0, payload.length);
        return (int) checksum.getValue();
    }
}
