package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.util.BlockContact;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * 1.8 self-placement: vanilla 1.8 excludes the placer from the placement collision check (place a ladder into your own
 * body); Minestom doesn't, so the block desyncs. Arms {@link OptimizedPlayer#setSelfPlacing} per placement - the placer
 * only, and only for PASSABLE blocks (stricter than 1.8: a motion-blocking block into your own hitbox is never a legit
 * clutch). Wraps the stock listener; an app that replaces the placement listener re-installs with it as the delegate, LAST.
 */
public final class LegacySelfPlacementFix {

    private LegacySelfPlacementFix() {}

    /** Installs the stock placement listener wrapped with the 1.8 self-placement exclusion. */
    public static void install() {
        install(BlockPlacementListener::listener);
    }

    /** Installs {@code delegate} wrapped with the exclusion - the composition seam for replaced placement listeners. */
    public static void install(@NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate) {
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientPlayerBlockPlacementPacket.class,
                wrap(delegate)::accept);
    }

    /** {@code delegate} wrapped with the exclusion but NOT installed - for hosts that own the listener slot, where
     *  registration order must not matter. */
    public static @NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> wrap(
            @NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate) {
        return (packet, player) -> wrapped(delegate, packet, player);
    }

    private static void wrapped(BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate,
                                ClientPlayerBlockPlacementPacket packet, Player player) {
        OptimizedPlayer op = player instanceof OptimizedPlayer o
                && excludesPlacer(player.getItemInHand(packet.hand()).material()) ? o : null;
        if (op != null) op.setSelfPlacing(true);
        try {
            delegate.accept(packet, player);
        } finally {
            if (op != null) op.setSelfPlacing(false);
        }
    }

    /** The {@code isBlock()} guard is required: a non-block item right-clicking a block has a {@code null} {@link Material#block()}. */
    private static boolean excludesPlacer(Material m) {
        return m.isBlock() && BlockContact.isPassable(m.block());
    }
}
