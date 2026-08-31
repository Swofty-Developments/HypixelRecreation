package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.listener.EntityActionListener;
import net.minestom.server.listener.PlayerActionListener;
import net.minestom.server.listener.PlayerInputListener;
import net.minestom.server.listener.UseItemListener;
import net.minestom.server.listener.manager.PacketPlayListenerConsumer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The self-echo meta fix's install hook: wraps the client-input packet listeners so an {@link OptimizedPlayer}
 * can tell client-initiated metadata changes from server-initiated ones (the flag it reads in
 * {@link OptimizedPlayer#sendPacketToViewersAndSelf}). Ported from the standalone {@code minestom-echo-fix}.
 *
 * <p>Inert unless the {@link OptimizedPlayer} provider is on - {@code Polyp} installs that, not this.
 * The tick-driven pose path (crawl enter/exit) is armed separately in {@link OptimizedPlayer#updatePose()}.
 */
public final class MetaFix {

    private static final AtomicBoolean installed = new AtomicBoolean(false);

    private MetaFix() {}

    /** Wraps the sneak / sprint / use-item / release-item listeners with the echo guard. Idempotent. */
    public static void installListeners() {
        if (!installed.compareAndSet(false, true)) return;

        wrapListener(ClientInputPacket.class, PlayerInputListener::listener);
        wrapListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        wrapListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        wrapListener(ClientPlayerActionPacket.class, PlayerActionListener::playerActionListener);
    }

    /**
     * Wraps a custom play listener with the client-input flag, so its self-bound metadata/attribute echoes are
     * filtered for an {@link OptimizedPlayer}. Use for listeners that mutate predicted player state.
     */
    public static <T extends ClientPacket> void wrapListener(
            Class<T> packetClass, PacketPlayListenerConsumer<@NotNull T> consumer) {
        MinecraftServer.getPacketListenerManager().setPlayListener(packetClass, (packet, player) -> {
            if (player instanceof OptimizedPlayer op) op.setProcessingClientInput(true);
            try {
                consumer.accept(packet, player);
            } finally {
                if (player instanceof OptimizedPlayer op) op.setProcessingClientInput(false);
            }
        });
    }
}
