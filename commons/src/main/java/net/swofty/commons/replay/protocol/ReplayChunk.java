package net.swofty.commons.replay.protocol;

import java.util.Arrays;

public record ReplayChunk(
        ReplaySection section,
        int sequence,
        int startTick,
        int endTick,
        int uncompressedLength,
        int recordCount,
        int checksum,
        byte[] compressedPayload
) {
    public ReplayChunk {
        if (section == null) throw new IllegalArgumentException("section is required");
        if (sequence < 0) throw new IllegalArgumentException("sequence must not be negative");
        if (startTick < 0 || endTick < startTick) throw new IllegalArgumentException("invalid tick range");
        if (uncompressedLength < 0 || recordCount < 0) throw new IllegalArgumentException("invalid chunk counts");
        compressedPayload = compressedPayload.clone();
    }

    @Override
    public byte[] compressedPayload() {
        return compressedPayload.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ReplayChunk chunk
                && section == chunk.section
                && sequence == chunk.sequence
                && startTick == chunk.startTick
                && endTick == chunk.endTick
                && uncompressedLength == chunk.uncompressedLength
                && recordCount == chunk.recordCount
                && checksum == chunk.checksum
                && Arrays.equals(compressedPayload, chunk.compressedPayload);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Objects.hash(section, sequence, startTick, endTick, uncompressedLength, recordCount, checksum)
                + Arrays.hashCode(compressedPayload);
    }
}
