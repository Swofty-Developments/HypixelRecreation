package io.github.term4.polyp.platform.player;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.network.packet.server.SendablePacket;
import org.jetbrains.annotations.NotNull;

/**
 * Delivers incremental slot/cursor refreshes bare: the {@link net.minestom.server.Viewable#sendPacketToViewers} default
 * wraps them in a shared {@code CachedPacket} the per-client item rewrite ({@link OptimizedPlayer#sendPacket}) can't
 * unwrap - so a stamp/reskin reverted after the first throw/drop until a full {@code WindowItems} resend. A player
 * inventory has one viewer; the shared cache buys nothing.
 */
public final class PerViewerInventory extends PlayerInventory {

    @Override
    public void sendPacketToViewers(@NotNull SendablePacket packet) {
        for (Player viewer : getViewers()) viewer.sendPacket(packet);
    }
}
