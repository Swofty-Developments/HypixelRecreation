package net.swofty.type.game.replay.event;

import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.game.replay.api.CoreReplayTypes;
import net.swofty.type.game.replay.api.ReplayEvent;
import net.swofty.type.game.replay.model.ReplayBlockPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoreReplayEventCodecTest {
    @Test
    void roundTripsEveryCoreEvent() throws Exception {
        List<ReplayEvent> events = List.of(
                new ReplayEntityAnimationEvent(41, ReplayEntityAnimationEvent.Animation.CRITICAL_EFFECT),
                new ReplayParticleEvent(new byte[]{1, 2, 3}),
                new ReplaySoundEvent("minecraft:entity.experience_orb.pickup", (byte) 1, 1, 2, 3, 0.8f, 1.2f),
                new ReplayBlockBreakEvent(9, new ReplayBlockPosition(-4, 70, 12), (byte) 5)
        );
        var registry = CoreReplayTypes.events();
        for (ReplayEvent event : events) {
            ReplayDataWriter writer = new ReplayDataWriter();
            event.write(writer);
            ReplayEvent decoded = registry.read(event.typeId(), writer.toByteArray());
            if (event instanceof ReplayParticleEvent expected) {
                assertEquals(List.of((byte) 1, (byte) 2, (byte) 3),
                        java.util.stream.IntStream.range(0, expected.packet().length)
                                .mapToObj(index -> expected.packet()[index]).toList());
                assertEquals(List.of((byte) 1, (byte) 2, (byte) 3),
                        java.util.stream.IntStream.range(0, ((ReplayParticleEvent) decoded).packet().length)
                                .mapToObj(index -> ((ReplayParticleEvent) decoded).packet()[index]).toList());
            } else {
                assertEquals(event, decoded);
            }
        }
    }

    @Test
    void rejectsUnknownRequiredEvent() {
        assertThrows(java.io.IOException.class, () -> CoreReplayTypes.events().read(9999, new byte[0]));
    }
}
