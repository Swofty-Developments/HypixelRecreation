package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.tracking.motion.VelocityRule;

/**
 * Vanilla 1.8 movement: the velocity tracking rule, set on a {@code MechanicsProfile.velocity(...)} scope rather than
 * per config. The player platform config is {@link Player}.
 */
public final class Movement {

    private Movement() {}

    /** The attacker self-slowdown on a landed sprint hit is not here - it's {@code AttackConfig.fullHitScale}. */
    public static VelocityRule velocity() {
        return VelocityRule.simulated();
    }
}
