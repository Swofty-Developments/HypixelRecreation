package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.presets.vanilla18.Explosion;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 1.8 chaining: a blast-destroyed TNT block primes (no drops) when the breaking config opts in. */
class TntChainTest extends HeadlessServerTest {

    @BeforeAll
    static void mechanics() {
        ExplosionSystem.install(polyp, Explosion.config());
    }

    @Test
    void aDestroyedTntBlockPrimesInsteadOfDropping() {
        AtomicReference<TntPrimeEvent> primed = new AtomicReference<>();
        var probe = EventListener.of(TntPrimeEvent.class, primed::set);
        MinecraftServer.getGlobalEventHandler().addListener(probe);
        BlockVec pos = new BlockVec(50, 65, 20);
        instance.setBlock(pos, Block.TNT);
        try {
            var cfg = BlockBreaking.builder().tntChain(true).build();
            List<Point> broken = ExplosionBlocks.destroy(MechanicsWorld.of(instance), List.of(pos), 4.0f, cfg, null);

            assertTrue(instance.getBlock(pos).air(), "the block converted");
            assertEquals(List.of(pos), broken);
            TntPrimeEvent event = primed.get();
            assertNotNull(event, "the prime fired through the event");
            assertEquals(TntPrimeEvent.Cause.EXPLOSION, event.cause());
            assertTrue(instance.getEntities().stream().anyMatch(e -> e instanceof PrimedTnt), "primed TNT spawned");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(probe);
        }
    }
}
