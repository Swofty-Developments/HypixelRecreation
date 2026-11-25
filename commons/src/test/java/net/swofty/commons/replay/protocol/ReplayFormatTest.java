package net.swofty.commons.replay.protocol;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplayFormatTest {
    @Test
    void roundTripsFramedChunk() throws Exception {
        List<byte[]> entries = List.of(new byte[]{1, 2, 3}, new byte[]{4, 5});
        ReplayChunk chunk = ReplayFormat.createChunk(ReplaySection.DELTA, 0, 12, 14, entries);

        List<byte[]> decoded = ReplayFormat.readChunk(chunk);

        assertEquals(entries.size(), decoded.size());
        assertArrayEquals(entries.get(0), decoded.get(0));
        assertArrayEquals(entries.get(1), decoded.get(1));
    }

    @Test
    void rejectsChecksumMismatch() throws Exception {
        ReplayChunk valid = ReplayFormat.createChunk(ReplaySection.EVENT, 0, 4, 4, List.of(new byte[]{9}));
        ReplayChunk corrupt = new ReplayChunk(valid.section(), valid.sequence(), valid.startTick(), valid.endTick(),
                valid.uncompressedLength(), valid.recordCount(), valid.checksum() + 1, valid.compressedPayload());

        assertThrows(IOException.class, () -> ReplayFormat.readChunk(corrupt));
    }

    @Test
    void rejectsMissingSequence() throws Exception {
        ReplayChunk first = ReplayFormat.createChunk(ReplaySection.SNAPSHOT, 0, 0, 0, List.of(new byte[]{1}));
        ReplayChunk third = ReplayFormat.createChunk(ReplaySection.SNAPSHOT, 2, 200, 200, List.of(new byte[]{2}));

        assertThrows(IOException.class, () -> ReplayFormat.validateOrdered(List.of(first, third), ReplaySection.SNAPSHOT));
    }
}
