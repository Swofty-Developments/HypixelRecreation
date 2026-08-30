package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 1.8's C08 at a block must fall back to USE the item (a bow draw aimed at the bridge floor); a modern
 * client sends its own follow-up USE_ITEM, so the fallback must never double-start for it.
 */
class LegacyUseOnBlockFixTest extends HeadlessServerTest {

    @BeforeAll
    static void installFix() {
        LegacyUseOnBlockFix.install(MinecraftServer.getGlobalEventHandler());
    }

    private static void useOnBlock(Player p) {
        EventDispatcher.call(new PlayerUseItemOnBlockEvent(p, PlayerHand.MAIN, p.getItemInMainHand(),
                new Vec(8, 40, 8), Vec.ZERO, BlockFace.TOP));
    }

    @Test
    void aLegacyBlockAimedDrawStartsTheUse() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "LegacyDraw").player;
        polyp.clientInfo().setProxyDetails(p, "{\"version\": 47}");
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        try {
            useOnBlock(p);
            assertNotNull(p.getItemUseHand(), "the 1.8 fallback must start the bow draw");
            assertEquals(PlayerHand.MAIN, p.getItemUseHand());
            for (int i = 0; i < 20; i++) p.tick(java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
            // a repeat C08 can only mean the client's draw RESTARTED (a drawing 1.8 client never re-sends):
            // the old span must never bank into the new draw
            useOnBlock(p);
            assertEquals(PlayerHand.MAIN, p.getItemUseHand());
            assertEquals(0, p.getCurrentItemUseTime(), "a repeat press restarts the draw clock");
        } finally {
            p.remove();
        }
    }

    @Test
    void aModernClientIsUntouched() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "ModernDraw").player;
        polyp.clientInfo().setProxyDetails(p, "{\"version\": 774}");
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        try {
            useOnBlock(p);
            assertNull(p.getItemUseHand(), "modern clients send their own USE_ITEM - no fallback");
        } finally {
            p.remove();
        }
    }
}
