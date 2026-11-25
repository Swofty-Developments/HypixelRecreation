package net.swofty.service.replay.session;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.replay.ReplayProtocolDto;
import net.swofty.commons.replay.protocol.ReplayChunk;
import net.swofty.commons.replay.protocol.ReplayFormat;
import net.swofty.commons.replay.protocol.ReplaySection;
import net.swofty.type.game.replay.codec.ReplaySnapshotCodec;
import net.swofty.type.game.replay.model.ReplaySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecordingSessionTest {
    @Test
    void acceptsAsynchronousChunksButRequiresACompleteSequence() throws Exception {
        RecordingSession session = session(400);
        ReplayChunk first = snapshotChunk(0, 0);
        ReplayChunk second = snapshotChunk(1, 200);
        ReplayChunk third = snapshotChunk(2, 400);

        session.addChunk(third);
        session.addChunk(first);
        assertThrows(java.io.IOException.class, session::validateComplete);

        session.addChunk(second);
        assertDoesNotThrow(session::validateComplete);
    }

    @Test
    void rejectsChecksumFailureBeforeQueuing() throws Exception {
        RecordingSession session = session(0);
        ReplayChunk valid = snapshotChunk(0, 0);
        ReplayChunk corrupt = new ReplayChunk(valid.section(), valid.sequence(), valid.startTick(), valid.endTick(),
                valid.uncompressedLength(), valid.recordCount(), valid.checksum() + 1, valid.compressedPayload());
        assertThrows(java.io.IOException.class, () -> session.addChunk(corrupt));
    }

    private RecordingSession session(int duration) {
        UUID replayId = UUID.randomUUID();
        var descriptor = new ReplayProtocolDto.Descriptor(replayId, "game", "BEDWARS", ServerType.BEDWARS_GAME,
                "server", "map", "hash", 0, 0, ReplayFormat.MAJOR_VERSION, 1, 0, duration, 0);
        var metadata = new ReplayProtocolDto.Metadata(descriptor, List.of(),
                new ReplayProtocolDto.GameMetadataEnvelope("BEDWARS", 2, new byte[0]));
        RecordingSession session = new RecordingSession(metadata);
        session.setDurationTicks(duration);
        return session;
    }

    private ReplayChunk snapshotChunk(int sequence, int tick) throws Exception {
        byte[] snapshot = ReplaySnapshotCodec.write(new ReplaySnapshot(tick, Map.of(), Map.of(), new byte[0]));
        return ReplayFormat.createChunk(ReplaySection.SNAPSHOT, sequence, tick, tick, List.of(snapshot));
    }
}
