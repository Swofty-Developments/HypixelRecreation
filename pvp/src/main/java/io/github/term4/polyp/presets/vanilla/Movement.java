package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.tracking.motion.ClimbModel;
import io.github.term4.polyp.tracking.motion.FluidFlow;
import io.github.term4.polyp.tracking.motion.VelocityConfig;
import io.github.term4.polyp.tracking.motion.VelocityRule;

/**
 * Modern (26.1) movement: the velocity tracking rule, set on a {@code MechanicsProfile.velocity(...)} scope.
 * (Modern player config is still TODO.)
 */
public final class Movement {

    private Movement() {}

    /** Modern fluid current: averaged + depth-scaled water and lava ({@link VelocityConfig#flowLava} on). */
    public static VelocityRule velocity() {
        return VelocityRule.simulated(VelocityConfig.builder()
                .flowModel(FluidFlow.Model.MODERN)
                .climbModel(ClimbModel.MODERN)
                .modernBlockPhysics(true)
                .build());
    }
}
