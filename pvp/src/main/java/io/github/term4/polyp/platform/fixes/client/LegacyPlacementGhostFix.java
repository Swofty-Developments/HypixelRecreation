package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;

/**
 * The vanilla 1.8 double block-update: after EVERY use-on-block, the 1.8 server re-asserts the clicked
 * AND the face-adjacent position ({@code PlayerConnection.a(PacketPlayInBlockPlace)}), success or refusal.
 * Modern servers replaced that with the sequence ack, which never reaches a 1.8 client - so a prediction
 * against a lagged phantom block (an instant-primed TNT) sticks forever. Restore the pair, from the
 * viewed world so shard players get shard truth.
 */
public final class LegacyPlacementGhostFix {

    private LegacyPlacementGhostFix() {}

    public static void install(EventNode<? super Event> node) {
        node.addListener(PlayerPacketEvent.class, e -> {
            if (!(e.getPacket() instanceof ClientPlayerBlockPlacementPacket packet)) return;
            Player p = e.getPlayer();
            Polyp polyp = Polyp.getInstance();
            if (polyp.clientInfo() == null || !polyp.clientInfo().isLegacy(p)) return;
            // end of tick: the placement (or its rejection) has been processed on every route
            MinecraftServer.getSchedulerManager().scheduleEndOfTick(() -> {
                if (!p.isOnline()) return;
                Instance instance = p.getInstance();
                if (instance == null) return;
                MechanicsWorld viewed = MechanicsWorld.viewed(p);
                Point clicked = packet.blockPosition();
                Point adjacent = clicked.relative(packet.blockFace());
                if (instance.isChunkLoaded(clicked)) {
                    p.sendPacket(new BlockChangePacket(clicked, viewed.getBlock(clicked)));
                }
                if (instance.isChunkLoaded(adjacent)) {
                    p.sendPacket(new BlockChangePacket(adjacent, viewed.getBlock(adjacent)));
                }
            });
        });
    }
}
