package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.tracking.motion.ClimbModel;
import io.github.term4.polyp.tracking.motion.FluidFlow;
import io.github.term4.polyp.tracking.motion.VelocityConfig;
import io.github.term4.polyp.tracking.motion.VelocityRule;

/** Hypixel's simulated-arc velocity rule; set ONCE on a {@code MechanicsProfile.velocity(...)} scope. */
public final class Movement {

    private Movement() {}

    /** Read by the melee friction fold, the hurt broadcast and projectile KB; configs leave their {@code velocity} unset. */
    public static VelocityRule velocity() {
        return VelocityRule.simulated(arcConfig());
    }

    private static VelocityConfig arcConfig() {
        return VelocityConfig.builder()
                // no vanilla motY < 0.005 apex reseed: with it the descending arc folds ~0.003 b/t low (~11 shorts by hit 3)
                .clampY(0)
                .entityPush(false) // player collision off: no server-side push residual to fold
                // modern horizontal current, 1.8 vertical water gravity (0.02): in-water KB is 0.39, not 0.3975
                .flowModel(FluidFlow.Model.MODERN.withLegacyWaterGravity())
                .flowLava(false) // water only, unlike vanilla 26
                .climbModel(ClimbModel.MODERN) // 1.8 LEGACY never fires climb-up server-side
                // motYOnMovePacket stays off: Hypixel's motY advances per server tick (the 1.9+ law), not 1.8's per-packet
                .build();
    }
}
