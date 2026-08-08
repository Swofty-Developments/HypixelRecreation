package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.platform.fixes.FixToggleConfig;
import io.github.term4.polyp.platform.fixes.FixesConfig;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 1.8 face douse (dig-start extinguishes fire on the clicked face, World.douseFire). */
class LegacyFireDouseFixTest extends HeadlessServerTest {

    private static final int FIZZ = 1009;
    private static FakePlayer miner;

    @BeforeAll
    static void install() {
        FixesSystem.install(polyp, FixesConfig.builder().legacyFireDouse(FixToggleConfig.on()).build());
        miner = FakePlayer.connect(instance, new Pos(20.5, 43, 20.5), "DouseMiner");
    }

    private static long fizzes() {
        return miner.sent(WorldEventPacket.class).stream().filter(p -> p.effectId() == FIZZ).count();
    }

    @Test
    void digStartDousesTheFireOnTheClickedFace() {
        BlockVec base = new BlockVec(20, 45, 22);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        long before = fizzes();
        EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, Block.STONE, base, BlockFace.TOP));
        assertTrue(instance.getBlock(base.add(0, 1, 0)).isAir(), "the fire on the clicked face is doused");
        assertEquals(Block.STONE, instance.getBlock(base), "the clicked block itself is untouched");
        assertEquals(before + 1, fizzes(), "one extinguish fizz");
    }

    @Test
    void digStartWithoutAdjacentFireDoesNothing() {
        BlockVec base = new BlockVec(24, 45, 22);
        instance.setBlock(base, Block.STONE);
        long before = fizzes();
        EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, Block.STONE, base, BlockFace.TOP));
        assertEquals(before, fizzes());
    }

    /** Creative insta-breaks skip StartDigging entirely; the douse rides the break event and CONSUMES the click. */
    @Test
    void creativeDouseConsumesTheClickAndKeepsTheBlock() {
        miner.player.setGameMode(GameMode.CREATIVE);
        try {
            BlockVec base = new BlockVec(28, 45, 22);
            instance.setBlock(base, Block.STONE);
            instance.setBlock(base.add(1, 0, 0), Block.FIRE);
            var breakEvent = new PlayerBlockBreakEvent(miner.player, instance, Block.STONE, Block.AIR, base, BlockFace.EAST);
            EventDispatcher.call(breakEvent);
            assertTrue(breakEvent.isCancelled(), "the douse consumes the creative click - the block survives");
            assertTrue(instance.getBlock(base.add(1, 0, 0)).isAir(), "the fire is doused");
        } finally {
            miner.player.setGameMode(GameMode.SURVIVAL);
        }
    }

    /** Vanilla 1.8 bails before the douse for adventure players (PlayerInteractManager.a). */
    @Test
    void adventureDigStartDoesNotDouse() {
        miner.player.setGameMode(GameMode.ADVENTURE);
        try {
            BlockVec base = new BlockVec(32, 45, 22);
            instance.setBlock(base, Block.STONE);
            instance.setBlock(base.add(0, 1, 0), Block.FIRE);
            long before = fizzes();
            EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, Block.STONE, base, BlockFace.TOP));
            assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "adventure never douses");
            assertEquals(before, fizzes());
        } finally {
            miner.player.setGameMode(GameMode.SURVIVAL);
        }
    }

    /** A protection listener's cancel (lobby maps) suppresses the douse. */
    @Test
    void cancelledDigStartDoesNotDouse() {
        BlockVec base = new BlockVec(20, 45, 26);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        long before = fizzes();
        var dig = new PlayerStartDiggingEvent(miner.player, instance, Block.STONE, base, BlockFace.TOP);
        dig.setCancelled(true);
        EventDispatcher.call(dig);
        assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "cancelled dig never douses");
        assertEquals(before, fizzes());
    }

    @Test
    void cancelledBreakDoesNotDouse() {
        BlockVec base = new BlockVec(24, 45, 26);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(1, 0, 0), Block.FIRE);
        long before = fizzes();
        var breakEvent = new PlayerBlockBreakEvent(miner.player, instance, Block.STONE, Block.AIR, base, BlockFace.EAST);
        breakEvent.setCancelled(true);
        EventDispatcher.call(breakEvent);
        assertFalse(instance.getBlock(base.add(1, 0, 0)).isAir(), "cancelled break never douses");
        assertEquals(before, fizzes());
    }
}
