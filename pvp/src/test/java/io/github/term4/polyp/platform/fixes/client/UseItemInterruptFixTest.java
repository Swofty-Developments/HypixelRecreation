package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.PlayerActionListener;
import net.minestom.server.listener.UseItemListener;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A used stack changing under the draw must stop the use silently (vanilla), so no stale draw survives to
 * fold into the next release; an identical re-set of the same material must NOT interrupt (a resync
 * rewriting the same bow keeps the client drawing too).
 */
class UseItemInterruptFixTest extends HeadlessServerTest {

    @BeforeAll
    static void installFix() {
        UseItemInterruptFix.install(MinecraftServer.getGlobalEventHandler());
    }

    private static void use(Player p) {
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 0,
                p.getPosition().yaw(), p.getPosition().pitch()), p);
    }

    private static void release(Player p) {
        PlayerActionListener.playerActionListener(new ClientPlayerActionPacket(
                ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE, Vec.ZERO, BlockFace.TOP, 0), p);
    }

    private static void tick(Player p) {
        EventDispatcher.call(new PlayerTickEvent(p));
    }

    @Test
    void aChangedStackStopsTheUseWithoutARelease() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "DrawSwap").player;
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        List<Long> cancels = new ArrayList<>();
        var listener = EventListener.of(PlayerCancelItemUseEvent.class, e -> cancels.add(e.getUseDuration()));
        MinecraftServer.getGlobalEventHandler().addListener(listener);
        try {
            use(p);
            assertNotNull(p.getItemUseHand());
            p.setItemInMainHand(ItemStack.of(Material.STONE)); // the resync/switch under the draw
            tick(p);
            assertNull(p.getItemUseHand(), "a changed stack must stop the use");
            release(p); // the client's release (if any) lands on nothing
            assertEquals(List.of(), cancels, "a silent stop never fires the release");

            // the next draw is fresh: it restamps, never spans
            p.setItemInMainHand(ItemStack.of(Material.BOW));
            use(p);
            assertNotNull(p.getItemUseHand());
            release(p);
            assertEquals(1, cancels.size());
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(listener);
            p.remove();
        }
    }

    @Test
    void anIdenticalResyncKeepsTheDraw() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "DrawSame").player;
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        try {
            use(p);
            p.setItemInMainHand(ItemStack.of(Material.BOW)); // InventorySync re-sending the same bow
            tick(p);
            assertNotNull(p.getItemUseHand(), "same material keeps the use, like the client keeps drawing");
        } finally {
            p.remove();
        }
    }

    @Test
    void aHotbarSwitchStopsTheUse() {
        Player p = FakePlayer.connect(instance, new Pos(8.5, 42, 8.5), "DrawScroll").player;
        p.setItemInMainHand(ItemStack.of(Material.BOW));
        try {
            use(p);
            p.setHeldItemSlot((byte) 3); // scroll mid-draw: the 1.8 client stops silently
            tick(p);
            assertNull(p.getItemUseHand(), "a held-slot change must stop the use");
        } finally {
            p.remove();
        }
    }
}
