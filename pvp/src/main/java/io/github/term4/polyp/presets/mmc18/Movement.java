package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.tracking.motion.VelocityRule;

/** MineMen velocity rule: the vanilla18 arc plus their tracker's broadcast vy floor. */
public final class Movement {

    private Movement() {}

    // broadcast-only, never the sim: |vy| < 0.05 goes out as sign*0.05 (0 -> +0.05); measured on KB (to -0.05
    // with the client then free-falling from the seed), projectile broadcasts and the grounded TNT +-0.05
    static final double WIRE_VY_FLOOR = 0.05;

    public static VelocityRule velocity() {
        var base = io.github.term4.polyp.presets.vanilla18.Movement.velocity().reconstructionConfig();
        return VelocityRule.simulated(base.toBuilder().wireFloorY(WIRE_VY_FLOOR).build());
    }
}
