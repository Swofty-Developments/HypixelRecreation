package io.github.term4.polyp.platform.player;

import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The blocking-pose stamp is per-VIEWER, so equipment has to reach each viewer BARE. A grouped send shares one
 * {@code CachedPacket}, which {@code PacketShapes.unwrapStateless} refuses to open, so the stamp never applied to an
 * equipment CHANGE - you could see someone block only if they already held the sword when they came into view.
 */
class EquipmentViewTransportTest extends HeadlessServerTest {

    @Test
    void aSwapToASwordReachesTheViewerStampedAndUncached() {
        var holder = FakePlayer.connect(instance, new Pos(300.5, 64, 300.5), "Blocker");
        var viewer = FakePlayer.connect(instance, new Pos(302.5, 64, 300.5), "Watcher");
        try {
            ((OptimizedPlayer) viewer.player).compat().apply(Compat18.config());
            assertTrue(holder.player.getViewers().contains(viewer.player), "the harness really is watching");
            viewer.sent.clear();

            holder.player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.DIAMOND_SWORD));

            // RAW captures: a CachedPacket here is the bug
            var bare = viewer.sent.stream()
                    .filter(EntityEquipmentPacket.class::isInstance).map(EntityEquipmentPacket.class::cast)
                    .filter(p -> p.entityId() == holder.player.getEntityId())
                    .filter(p -> p.equipments().containsKey(EquipmentSlot.MAIN_HAND))
                    .toList();

            assertTrue(!bare.isEmpty(), "the swap must reach the viewer as a bare packet, not a shared CachedPacket");
            assertNotNull(bare.getLast().equipments().get(EquipmentSlot.MAIN_HAND).get(DataComponents.BLOCKS_ATTACKS),
                    "and carry the stamp, or the viewer can never render the block pose");
        } finally {
            viewer.player.remove();
            holder.player.remove();
        }
    }

    /** No viewer rewrites items -> keep Minestom's grouped/cached fast path. */
    @Test
    void plainViewersKeepTheGroupedFastPath() {
        var holder = FakePlayer.connect(instance, new Pos(310.5, 64, 310.5), "PlainHolder");
        var viewer = FakePlayer.connect(instance, new Pos(312.5, 64, 310.5), "PlainWatcher");
        try {
            ((OptimizedPlayer) viewer.player).compat().apply(null);
            viewer.sent.clear();

            holder.player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.DIAMOND_SWORD));

            assertTrue(viewer.sent.stream().noneMatch(EntityEquipmentPacket.class::isInstance),
                    "nothing to rewrite -> the packet still groups (arrives wrapped)");
        } finally {
            viewer.player.remove();
            holder.player.remove();
        }
    }
}
