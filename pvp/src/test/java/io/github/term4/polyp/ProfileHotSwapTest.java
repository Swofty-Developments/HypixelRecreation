package io.github.term4.polyp;

import io.github.term4.polyp.platform.fixes.FixToggleConfig;
import io.github.term4.polyp.platform.fixes.FixesConfig;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Profiles hot-swap at runtime: a scope assignment flips behavior on the next event, no reinstall. */
class ProfileHotSwapTest extends HeadlessServerTest {

    private static FakePlayer miner;

    @BeforeAll
    static void install() {
        FixesSystem.install(polyp, FixesConfig.builder().build()); // douse off at install
        miner = FakePlayer.connect(instance, new Pos(52.5, 43, 20.5), "SwapMiner");
    }

    private static void dig(BlockVec base) {
        EventDispatcher.call(new PlayerStartDiggingEvent(miner.player, instance, Block.STONE, base, BlockFace.TOP));
    }

    @Test
    void playerProfileSwapFlipsTheDouseFixLive() {
        BlockVec base = new BlockVec(52, 45, 22);
        instance.setBlock(base, Block.STONE);
        instance.setBlock(base.add(0, 1, 0), Block.FIRE);

        dig(base);
        assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "install config: fix off");

        polyp.profiles().setPlayer(miner.player, MechanicsKeys.FIXES,
                FixesConfig.builder().legacyFireDouse(FixToggleConfig.on()).build());
        dig(base);
        assertFalse(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "profile swap enables it on the next event");

        instance.setBlock(base.add(0, 1, 0), Block.FIRE);
        polyp.profiles().setPlayer(miner.player, MechanicsKeys.FIXES, null);
        dig(base);
        assertTrue(instance.getBlock(base.add(0, 1, 0)).compare(Block.FIRE), "clearing the member falls back to the install config");
    }
}
