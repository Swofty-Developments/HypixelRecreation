package net.swofty.type.game.replay.codec;

import net.swofty.type.game.replay.model.ReplayBlockPosition;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayPotionEffectState;
import net.swofty.type.game.replay.model.ReplaySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplaySnapshotCodecTest {
    @Test
    void roundTripsCompleteSnapshot() throws Exception {
        UUID uuid = UUID.randomUUID();
        ReplayEntityState entity = new ReplayEntityState(
                42, uuid, 1, 1.25, 70, -3.5, 90, 10, 0.1, 0.2, 0.3,
                0, true, true, 7, ReplayEntityState.Lifecycle.ALIVE,
                Map.of(0, new byte[]{1, 2}), 17, 20,
                List.of(new ReplayPotionEffectState(1, (byte) 2, 40, (byte) 6)),
                new ReplayEntityState.PlayerState(uuid, "texture", "signature", "{\"text\":\"Player\"}",
                        "RED", 0, false, new byte[]{3}), new byte[]{4, 5});
        ReplaySnapshot snapshot = new ReplaySnapshot(200,
                Map.of(new ReplayBlockPosition(4, 80, -2), 123), Map.of(42, entity), new byte[]{6, 7});

        assertEquals(snapshot, ReplaySnapshotCodec.read(ReplaySnapshotCodec.write(snapshot)));
    }
}
