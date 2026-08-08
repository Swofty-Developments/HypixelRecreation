package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.motion.LegacyVelocity;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Byte-exact 1.8 velocity, bypassing the lossy 26.1 LpVec3 wire, by client transport:
 * <ul>
 *   <li><b>1.8/Via</b> - {@link #applyExact} injects the knocked player's exact short through {@link ViaBridgeRpc} (the proxy
 *   translates it losslessly), only above 2 b/t where {@link LegacyVelocity#snap} drifts; availability is cached per player, the lossy snap is the fallback.</li>
 *   <li><b>Animatium</b> ({@link AnimatiumFeature#SHORTS_VELOCITY}) - the client decodes EVERY velocity packet as a 1.8 short, so the
 *   {@link PlayerPacketOutEvent} listener rewrites all of its {@link EntityVelocityPacket}s to shorts via {@link LegacyShortVelocity} (what Via does for a real 1.8 client).</li>
 * </ul>
 */
public final class LegacyVelocityBridge {

    /** Set just before {@code setVelocity} on the ViaBridge path; the listener cancels the one self LpVec3 echo it gates. */
    private static final Tag<Boolean> SUPPRESS_SELF_VELOCITY = Tag.Transient("polyp:bridge-suppress-self-velocity");

    private enum Availability { UNKNOWN, AVAILABLE, UNAVAILABLE }
    private static final Map<UUID, Availability> AVAILABILITY = new ConcurrentHashMap<>();

    private static volatile Polyp polyp;

    private LegacyVelocityBridge() {}

    public static void install(@NotNull Polyp instance) {
        polyp = instance;
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:legacy-velocity-bridge", EventFilter.PLAYER);
        node.addListener(PlayerPacketOutEvent.class, LegacyVelocityBridge::onVelocityOut);
        node.addListener(PlayerDisconnectEvent.class, e -> AVAILABILITY.remove(e.getPlayer().getUuid()));
        polyp.install(node);
    }

    private static void onVelocityOut(PlayerPacketOutEvent e) {
        if (!(e.getPacket() instanceof EntityVelocityPacket vel)) return;
        Player p = e.getPlayer();

        // gated on advertised decoder support so an ALL-features client lacking the decoder is untouched;
        // the BufferedPacket send won't re-fire this event
        if (p instanceof OptimizedPlayer op
                && op.compat().handlesNatively(AnimatiumFeature.SHORTS_VELOCITY)
                && op.compat().supports(AnimatiumFeature.SHORTS_VELOCITY)) {
            e.setCancelled(true);
            // vel.velocity() is b/t (getVelocityForPacket divides by tps); wireShorts wants b/s. Clamp at the 1.8 cap, like Via.
            short[] s = LegacyVelocity.wireShorts(vel.velocity().mul(ServerFlag.SERVER_TICKS_PER_SECOND), LegacyVelocity.DEFAULT_CAP);
            LegacyShortVelocity.send(p, vel.entityId(), s[0], s[1], s[2]);
            return;
        }

        // ViaBridge path: drop the one tagged self LpVec3 echo (the exact short goes via the proxy RPC instead)
        if (Boolean.TRUE.equals(p.getTag(SUPPRESS_SELF_VELOCITY)) && vel.entityId() == p.getEntityId()) {
            e.setCancelled(true);
            p.removeTag(SUPPRESS_SELF_VELOCITY);  // one-shot
        }
    }

    /**
     * For a knocked 1.8/Via player above the LP-exact band, injects the byte-exact 1.8 short over ViaBridge (suppressing the
     * lossy LP self echo) and returns {@code true} (having already called {@code setVelocity}). {@code false} = not applicable
     * (incl. Animatium clients, whose velocity the {@link #install outgoing listener} rewrites wholesale), so the caller broadcasts normally.
     */
    public static boolean applyExact(@NotNull Entity target, @NotNull Vec rawBps, @NotNull Vec snappedBps, double capBt) {
        if (polyp == null || !ViaBridgeRpc.isInstalled() || !(target instanceof Player player)) return false;
        if (!polyp.clientInfo().isLegacy(player) || !LegacyVelocity.exceedsLpExactBand(rawBps)) return false;
        if (AVAILABILITY.getOrDefault(player.getUuid(), Availability.UNKNOWN) == Availability.UNAVAILABLE) return false;

        short[] s = LegacyVelocity.wireShorts(rawBps, capBt);
        player.setTag(SUPPRESS_SELF_VELOCITY, true);
        target.setVelocity(snappedBps); // viewers get the (imperceptible) LP value; the self echo is suppressed above
        ViaBridgeRpc.get()
                .sendEntityMotion(player, ViaBridgeRpc.PROTOCOL_1_21_6, player.getEntityId(), s[0], s[1], s[2])
                .whenComplete((ignored, err) -> {
                    AVAILABILITY.put(player.getUuid(), err == null ? Availability.AVAILABLE : Availability.UNAVAILABLE);
                    // bridge missing/failed: resend the LpVec3 value we suppressed (next tick, on the entity thread)
                    if (err != null && player.isOnline()) {
                        player.scheduleNextTick(en -> { en.removeTag(SUPPRESS_SELF_VELOCITY); en.setVelocity(snappedBps); });
                    }
                });
        return true;
    }
}
