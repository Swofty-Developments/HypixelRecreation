package io.github.term4.polyp.platform;

import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Packet unwrapping for every-outgoing-packet rewrite paths (equipment strip, attack-range stamp, throwable reskin).
 *
 * <p>Resolves only the state-independent shape: a bare {@link ServerPacket}. NEVER route these
 * paths through {@code extractServerPacket}: its {@code CachedPacket} branch forces that packet's single, stateless
 * {@code FramedPacket} cache for whatever connection state is passed - a hardcoded PLAY poisons the cache during a
 * modern client's CONFIGURATION join and corrupts the real config send. {@code CachedPacket.updatedCache} returns an
 * existing cache without comparing states, so the first caller's state sticks; passing "the right" state is no fix.
 *
 * <p>The cost is that a GROUPED send ({@code sendPacketToViewers} wraps every packet) is invisible here, so a
 * per-viewer rewrite silently no-ops on it. Senders that need one must deliver bare - {@code PerViewerInventory} for
 * slot refreshes, {@code OptimizedPlayer.sendPacketToViewers} for equipment. A viewer-independent rewrite can instead
 * run sender-side before grouping ({@code EquipmentSlotsFix}). Miss that and the bug reads as intermittent: the
 * new-viewer send is bare and rewrites, the change broadcast is cached and does not.
 */
public final class PacketShapes {

    private PacketShapes() {}

    /** The unwrapped {@link ServerPacket}, or {@code null} for shapes that can't be resolved statelessly. */
    public static @Nullable ServerPacket unwrapStateless(@NotNull SendablePacket packet) {
        return packet instanceof ServerPacket sp ? sp : null;
    }
}
