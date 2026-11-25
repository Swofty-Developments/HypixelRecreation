package io.github.term4.polyp.platform.player;

import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.testsupport.CapturingConnection;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.player.GameProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * An incremental slot refresh must reach the client bare (not a shared {@code CachedPacket}) or the outgoing
 * reskin/stamp never runs on it - the fix for the swing/attack-range reverting after the first throw or drop.
 */
class PerViewerInventoryTest extends HeadlessServerTest {

    @Test
    void incrementalSlotRefreshReachesClientReskinned() {
        var conn = new CapturingConnection();
        OptimizedPlayer p = new OptimizedPlayer(conn, new GameProfile(UUID.randomUUID(), "InvTest"));
        p.compat().apply(Compat18.config()); // suppressThrowSwing on
        assertInstanceOf(PerViewerInventory.class, p.getInventory());
        p.getInventory().addViewer(p); // a real join adds the owner at spawn

        conn.sent.clear();
        p.getInventory().setItemStack(0, ItemStack.of(Material.SNOWBALL)); // the throw/drop path

        SendablePacket slot = conn.sent.stream()
                .filter(x -> x instanceof SetPlayerInventorySlotPacket || x instanceof SetSlotPacket)
                .findFirst().orElseThrow(() -> new AssertionError("no slot refresh reached the client"));
        ItemStack shown = slot instanceof SetPlayerInventorySlotPacket sp ? sp.itemStack() : ((SetSlotPacket) slot).itemStack();
        assertEquals(Material.PAPER, shown.material(), "the incremental slot refresh is reskinned, not reverted to a raw snowball");
    }
}
