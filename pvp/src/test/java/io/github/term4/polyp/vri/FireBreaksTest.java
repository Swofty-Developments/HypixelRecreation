package io.github.term4.polyp.vri;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fire parity on breaks: the direct-break fizz + silent orphaned-fire removal. */
class FireBreaksTest extends HeadlessServerTest {

    private static final int FIZZ = 1009;
    private static FakePlayer miner;

    @BeforeAll
    static void install() {
        Vri.install(polyp, VriConfig.builder().fireBreaks(true).build());
        miner = FakePlayer.connect(instance, new Pos(20.5, 43, 30.5), "FireBreaker");
    }

    private static long fizzes() {
        return miner.sent(WorldEventPacket.class).stream().filter(p -> p.effectId() == FIZZ).count();
    }

    @Test
    void directFireBreakFizzes() {
        BlockVec pos = new BlockVec(20, 45, 30);
        instance.setBlock(pos, Block.FIRE);
        long before = fizzes();
        EventDispatcher.call(new PlayerBlockBreakEvent(miner.player, instance, Block.FIRE, Block.AIR, pos, BlockFace.TOP));
        assertEquals(before + 1, fizzes(), "the modern instabreak path fizzes (BaseFireBlock.playerWillDestroy)");
        instance.setBlock(pos, Block.AIR);
    }

    /** Breaking the SUPPORT from a side face orphans the fire above: removed silently (vanilla's neighbor update). */
    @Test
    void breakingTheSupportRemovesTheOrphanedFireSilently() {
        BlockVec base = new BlockVec(24, 45, 30);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        long before = fizzes();
        instance.setBlock(base, Block.AIR); // the break event's world state: support already gone
        EventDispatcher.call(new PlayerBlockBreakEvent(miner.player, instance, Block.STONE, Block.AIR, base, BlockFace.NORTH));
        assertTrue(instance.getBlock(base.add(0, 1, 0)).isAir(), "the floating fire is removed");
        assertEquals(before, fizzes(), "support-loss removal is silent");
    }

    /** Vanilla keeps unsupported fire alive off a flammable neighbor (FireBlock.canSurvive / 1.8 canPlace). */
    @Test
    void flammableNeighborKeepsTheFireWhenTheFloorBreaks() {
        BlockVec base = new BlockVec(20, 45, 34);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        instance.setBlock(base.add(1, 1, 0), Block.OAK_PLANKS); // beside the fire, not under it
        EventDispatcher.call(new PlayerBlockBreakEvent(miner.player, instance, Block.STONE, Block.AIR, base, BlockFace.NORTH));
        assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "fire clings to the flammable neighbor");
    }

    /** Fire on the SIDE of a flammable wall (no floor) dies with the wall. */
    @Test
    void sideAttachedFireFallsWhenItsWallBreaks() {
        BlockVec wall = new BlockVec(24, 45, 34);
        BlockVec fire = wall.add(1, 0, 0);
        instance.setBlock(wall, Block.OAK_PLANKS);
        instance.setBlock(fire, Block.FIRE);
        instance.setBlock(fire.add(0, -1, 0), Block.AIR); // nothing sturdy under the fire
        EventDispatcher.call(new PlayerBlockBreakEvent(miner.player, instance, Block.OAK_PLANKS, Block.AIR, wall, BlockFace.NORTH));
        assertTrue(instance.getBlock(fire).isAir(), "the wall was the fire's only support");
    }

    /** Soul fire only lives on soul sand/soil - a flammable neighbor does not save it. */
    @Test
    void soulFireDiesWithItsSoulBase() {
        BlockVec base = new BlockVec(28, 45, 34);
        instance.setBlock(base, Block.SOUL_SAND);
        instance.setBlock(base.add(0, 1, 0), Block.SOUL_FIRE);
        instance.setBlock(base.add(1, 1, 0), Block.OAK_PLANKS);
        EventDispatcher.call(new PlayerBlockBreakEvent(miner.player, instance, Block.SOUL_SAND, Block.AIR, base, BlockFace.NORTH));
        assertTrue(instance.getBlock(base.add(0, 1, 0)).isAir(), "soul fire needs its soul base");
    }

    /** A protection listener's cancel (lobby maps) keeps the fire and stays silent. */
    @Test
    void cancelledBreakLeavesTheFireAlone() {
        BlockVec base = new BlockVec(28, 45, 30);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        long before = fizzes();
        var breakEvent = new PlayerBlockBreakEvent(miner.player, instance, Block.STONE, Block.AIR, base, BlockFace.NORTH);
        breakEvent.setCancelled(true);
        EventDispatcher.call(breakEvent);
        assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "cancelled break keeps the fire");
        assertEquals(before, fizzes());
    }
}
