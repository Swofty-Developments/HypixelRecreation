package test;

import io.github.term4.polyp.platform.compatibility.ViaBridgeException;
import io.github.term4.polyp.platform.compatibility.ViaBridgeRpc;
import io.github.term4.polyp.tracking.motion.LegacyVelocity;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

final class VelocityCapTestCommands {

    private static final double CAP_BPT = 3.9;
    private static final short CAP_SHORT = 31200;
    private static final Tag<Boolean> SUPPRESS_VELOCITY_PACKET = Tag.Transient("polyp:test:suppress-velocity-packet");

    private VelocityCapTestCommands() {}

    static void install(@NotNull EventNode<@NotNull PlayerEvent> playerNode) {
        playerNode.addListener(PlayerPacketOutEvent.class, event -> {
            if (!Boolean.TRUE.equals(event.getPlayer().getTag(SUPPRESS_VELOCITY_PACKET))) return;
            if (event.getPacket() instanceof EntityVelocityPacket vel
                    && vel.entityId() == event.getPlayer().getEntityId()) {
                event.setCancelled(true);
            }
        });
    }

    static void applyNormalCap(@NotNull Player player) {
        Vec snapped = LegacyVelocity.snap(capPerSecond());
        player.setVelocity(snapped);
        player.sendMessage("[velcap] normal setVelocity X="
                + formatBpt(snapped.x())
                + " b/s (snapped). On 1.8 via LpVec3 expect ~3.899875 b/t, not 3.9.");
    }

    static void applyBridgeCap(@NotNull Player player) {
        Vec snapped = LegacyVelocity.snap(capPerSecond());
        player.setTag(SUPPRESS_VELOCITY_PACKET, true);
        player.setVelocity(snapped);

        ViaBridgeRpc.get().sendEntityMotion(
                player,
                ViaBridgeRpc.PROTOCOL_1_21_6,
                player.getEntityId(),
                CAP_SHORT,
                (short) 0,
                (short) 0
        ).whenComplete((ignored, err) -> {
            player.scheduleNextTick(entity -> entity.removeTag(SUPPRESS_VELOCITY_PACKET));
            if (!player.isOnline()) return;
            if (err == null) {
                player.sendMessage("[velcapbridge] ViaBridge short X=" + CAP_SHORT
                        + " (3.9 b/t). 1.8 client should read exactly 3.9.");
                return;
            }
            String detail = err instanceof ViaBridgeException ? err.getMessage() : err.toString();
            player.sendMessage("[velcapbridge] failed: " + detail
                    + " - is ViaBridge on the Velocity proxy?");
        });
    }

    private static Vec capPerSecond() {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        return new Vec(CAP_BPT * tps, 0, 0);
    }

    private static String formatBpt(double blocksPerSecond) {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        return String.format("%.6f", blocksPerSecond / tps);
    }
}
