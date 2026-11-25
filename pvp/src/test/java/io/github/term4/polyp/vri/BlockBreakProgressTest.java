package io.github.term4.polyp.vri;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerCancelDiggingEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.utils.block.BlockBreakCalculation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The break-progress broadcast against synthesized instance ticks - the headless harness has no ticker. */
class BlockBreakProgressTest extends HeadlessServerTest {

    private static final BlockVec BLOCK = new BlockVec(2, 63, 2);

    private static FakePlayer miner;
    private static FakePlayer viewer;

    @BeforeAll
    static void setUp() {
        Vri.install(polyp, VriConfig.builder().blockBreakProgress(true).build());
        miner = FakePlayer.connect(instance, new Pos(0.5, 64, 0.5), "Miner");
        viewer = FakePlayer.connect(instance, new Pos(5.5, 64, 5.5), "Viewer");
    }

    @AfterAll
    static void tearDown() {
        miner.player.remove();
        viewer.player.remove();
    }

    @Test
    void broadcastsStagesToViewerNotMiner() {
        int breakTicks = BlockBreakCalculation.breakTicks(instance.getBlock(BLOCK), miner.player);
        assertTrue(breakTicks > 10, "stone by hand must be a multi-stage dig");
        int stage1Boundary = breakTicks / 10; // stage = (ticksSpent+1)*10/breakTicks

        viewer.sent.clear();
        miner.sent.clear();
        EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, instance.getBlock(BLOCK), BLOCK, BlockFace.TOP));

        List<BlockBreakAnimationPacket> packets = viewer.sent(BlockBreakAnimationPacket.class);
        assertEquals(1, packets.size());
        assertEquals(new BlockBreakAnimationPacket(miner.player.getEntityId(), BLOCK, (byte) 0), packets.getFirst());

        for (int i = 0; i < stage1Boundary; i++) EventDispatcher.call(new InstanceTickEvent(instance, 0, 0));
        packets = viewer.sent(BlockBreakAnimationPacket.class);
        assertEquals(2, packets.size(), "one packet per stage change, deduped in between");
        assertEquals((byte) 1, packets.get(1).destroyStage());

        EventDispatcher.call(new PlayerCancelDiggingEvent(miner.player, instance, instance.getBlock(BLOCK), BLOCK));
        packets = viewer.sent(BlockBreakAnimationPacket.class);
        assertEquals((byte) -1, packets.getLast().destroyStage(), "abort must clear the crack overlay");

        assertEquals(List.of(), miner.sent(BlockBreakAnimationPacket.class), "the miner predicts its own cracks");
    }

    @Test
    void domainMinersCrackOnTheirOwnClock() {
        instance.setBlock(BLOCK.blockX(), BLOCK.blockY(), BLOCK.blockZ(), Block.STONE);
        long[] clock = {100};
        MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
            @Override public MechanicsWorld resolve(Entity e) { return e.getTag(MechanicsWorld.ENTITY_TAG); }
            @Override public boolean externallyTicked(Entity e) { return e == miner.player; }
            @Override public long externalTick(Entity e) { return e == miner.player ? clock[0] : -1; }
        });
        try {
            int breakTicks = BlockBreakCalculation.breakTicks(instance.getBlock(BLOCK), miner.player);
            int stage1Boundary = breakTicks / 10;
            EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, instance.getBlock(BLOCK), BLOCK, BlockFace.TOP));
            viewer.sent.clear();

            for (int i = 0; i < stage1Boundary + 3; i++) EventDispatcher.call(new InstanceTickEvent(instance, 0, 0));
            assertEquals(List.of(), viewer.sent(BlockBreakAnimationPacket.class),
                    "the main pass leaves a domain miner's dig alone");

            clock[0] += stage1Boundary;
            TickSystem.tickWorld(MechanicsWorld.of(instance), clock[0]);
            List<BlockBreakAnimationPacket> packets = viewer.sent(BlockBreakAnimationPacket.class);
            assertEquals(1, packets.size(), "the world pass advances the crack on the miner's clock");
            assertEquals((byte) 1, packets.getFirst().destroyStage());
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            EventDispatcher.call(new PlayerCancelDiggingEvent(miner.player, instance, instance.getBlock(BLOCK), BLOCK));
        }
    }

    @Test
    void blockGoneClearsWithoutFinish() {
        instance.setBlock(BLOCK.blockX(), BLOCK.blockY(), BLOCK.blockZ(), Block.STONE);
        EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, instance.getBlock(BLOCK), BLOCK, BlockFace.TOP));
        viewer.sent.clear();

        instance.setBlock(BLOCK.blockX(), BLOCK.blockY(), BLOCK.blockZ(), Block.AIR);
        EventDispatcher.call(new InstanceTickEvent(instance, 0, 0));

        List<BlockBreakAnimationPacket> packets = viewer.sent(BlockBreakAnimationPacket.class);
        assertEquals(1, packets.size());
        assertEquals((byte) -1, packets.getFirst().destroyStage());
        instance.setBlock(BLOCK.blockX(), BLOCK.blockY(), BLOCK.blockZ(), Block.STONE);
    }
}
