package io.github.term4.polyp.vri;

import io.github.term4.polyp.world.FireSupport;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Fire parity on block breaks - Minestom runs neither piece: breaking fire directly fizzes
 * ({@code BaseFireBlock.playerWillDestroy}, world event 1009), and fire the break leaves unsupported is removed
 * SILENTLY ({@link FireSupport}). The 1.8 face douse is a client fix ({@code LegacyFireDouseFix}).
 */
final class FireBreaks {

    private FireBreaks() {}

    static void install(EventNode<@NotNull Event> node, Vri vri) {
        node.addListener(PlayerBlockBreakEvent.class, e -> {
            if (e.isCancelled() || !vri.configFor(e.getPlayer()).fireBreaks) return;
            MechanicsWorld world = MechanicsWorld.of(e.getPlayer());
            if (FireSupport.isFire(e.getBlock())) {
                world.broadcast(new WorldEventPacket(FireSupport.FIZZ, e.getBlockPosition(), 0, false));
            }
            FireSupport.sweep(world, e.getBlockPosition());
        });
    }
}
