package net.swofty.type.game.replay.codec;

import net.swofty.commons.ServerType;
import net.swofty.type.game.replay.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplayHeaderCodecTest {
    @Test
    void roundTripsHeaderAndSnapshotIndex() throws Exception {
        UUID replayId = UUID.randomUUID();
        ReplayDescriptor descriptor = new ReplayDescriptor(replayId, "game", "BEDWARS", ServerType.BEDWARS_GAME,
                "server", "map", "hash", 2, 3, 4, 100, 200, 400, 500);
        ReplayParticipant participant = new ReplayParticipant(UUID.randomUUID(), 10, "Player", null, null,
                "{\"text\":\"Player\"}", "{\"text\":\"\"}", "{\"text\":\"\"}");
        ReplayHeader header = new ReplayHeader(new ReplayMetadata(descriptor, List.of(participant),
                new ReplayGameMetadataEnvelope("BEDWARS", 1, new byte[]{1, 2})), List.of(0, 200, 400));

        assertEquals(header, ReplayHeaderCodec.read(ReplayHeaderCodec.write(header)));
    }

    @Test
    void rejectsBadMagic() throws Exception {
        ReplayDescriptor descriptor = new ReplayDescriptor(UUID.randomUUID(), "game", "BEDWARS", ServerType.BEDWARS_GAME,
                "server", "map", "hash", 0, 0, 4, 1, 2, 0, 0);
        ReplayHeader header = new ReplayHeader(new ReplayMetadata(descriptor, List.of(),
                new ReplayGameMetadataEnvelope("BEDWARS", 1, new byte[0])), List.of(0));
        byte[] encoded = ReplayHeaderCodec.write(header);
        encoded[0] ^= 1;

        assertThrows(IOException.class, () -> ReplayHeaderCodec.read(encoded));
    }
}
