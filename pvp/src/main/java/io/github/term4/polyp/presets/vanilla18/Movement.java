package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.tracking.motion.VelocityConfig;
import io.github.term4.polyp.tracking.motion.VelocityRule;

/**
 * Vanilla 1.8 movement: the velocity tracking rule, set on a {@code MechanicsProfile.velocity(...)} scope rather than
 * per config. The player platform config is {@link Player}.
 */
public final class Movement {

    private Movement() {}

    /** The attacker self-slowdown on a landed sprint hit is not here - it's {@code AttackConfig.fullHitScale}. */
    public static VelocityRule velocity() {
        // per-packet motY: 1.8 runs the player's living tick (travel gravity included) from the flying-packet
        // handler (PlayerConnection.a(PacketPlayInFlying) -> l(), its only caller); 1.9+ moved it to the server tick
        return VelocityRule.simulated(VelocityConfig.builder().motYOnMovePacket(true).build());
    }
}
