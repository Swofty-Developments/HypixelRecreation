package io.github.term4.polyp.tracking.motion;

import org.jetbrains.annotations.Nullable;

/**
 * Knobs for the {@link VelocityRule#simulated(VelocityConfig) simulated} server-tracked velocity. Plain values
 * (per-context conditionality lives one level up). Gravity/drag/friction are read live from the entity, not here.
 * Build with {@link #builder()}.
 *
 * @param seed            fallback takeoff motY; the ticked sim always uses {@link #JUMP_VELOCITY}.
 * @param launchOffset    arc phase correction; debug knob.
 * @param zeroBelowY      vanilla's living-tick zeroing: {@code |motY|} under it becomes 0 (the apex reseed); {@code 0} = off.
 * @param groundTicks     fall-prediction depth for {@link MotionTracker#onGround} ({@code 0} = raw client flag).
 * @param maxAirTicks     fallback only: caps the air clock; {@code null} = unbounded.
 * @param entityPush      fold the {@code Entity.collide} push residual; disable where player collision is off.
 * @param fluidPhysics    water/lava drag + buoyancy; off also disables {@link #flowPush}.
 * @param webPhysics      cobweb handling - zeroes motion.
 * @param flowPush        fold the water-flow current residual (simulated rules only).
 * @param flowLava        whether MODERN flow also pushes in lava (26 yes, Hypixel no); no effect on LEGACY.
 * @param modernBlockPhysics 26-only block velocity (sweet-berry/powder-snow stuck + bed bounce).
 * @param motYOnMovePacket advance the motY sim only on ticks with a client move packet, so a lag-frozen victim's
 *                         motY holds until its next move - the 1.8 server law ({@code PlayerConnection} drives the
 *                         player's living tick per flying packet); off = every server tick (1.9+/Hypixel).
 * @param wireFloorY      broadcast-only magnitude floor: a wire vy under it goes out as {@code sign*floor} (0 up);
 *                        {@code null} = off. Read from scope by knockback, projectile and TNT sends alike; the sim
 *                        is untouched. X/Z: the same law per axis.
 */
public record VelocityConfig(
        double seed,
        int launchOffset,
        double zeroBelowX,
        double zeroBelowY,
        double zeroBelowZ,
        int groundTicks,
        @Nullable Integer maxAirTicks,
        boolean entityPush,
        boolean fluidPhysics,
        boolean climbPhysics,
        boolean webPhysics,
        boolean flowPush,
        FluidFlow.Model flowModel,
        boolean flowLava,
        ClimbModel climbModel,
        boolean modernBlockPhysics,
        boolean motYOnMovePacket,
        @Nullable Double wireFloorX,
        @Nullable Double wireFloorY,
        @Nullable Double wireFloorZ
) {

    /** Vanilla {@code motY -= 0.08} (b/t^2). */
    public static final double GRAVITY = 0.08;
    /** Vanilla {@code motY *= 0.98}. */
    public static final double DRAG_V = 0.98;
    /** Vanilla {@code motX/motZ *= 0.91} airborne. */
    public static final double DRAG_H = 0.91;
    /** {@code bF()}'s {@code 0.42F} widened to double - float-exact for the hurt-broadcast wire short. */
    public static final double JUMP_VELOCITY = 0.41999998688697815;
    /** Vanilla {@code m()} zeroes {@code |mot| < 0.005} each tick. */
    public static final double ZERO_BELOW = 0.005;

    /** The hit packet is processed one tick before the victim's move, so the fold reads {@code ticksInAir - 1}. */
    public static final int DEFAULT_LAUNCH_OFFSET = -1;

    public static VelocityConfig defaults() { return builder().build(); }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private double seed = JUMP_VELOCITY;
        private int launchOffset = DEFAULT_LAUNCH_OFFSET;
        private double zeroBelowX = ZERO_BELOW;
        private double zeroBelowY = ZERO_BELOW;
        private double zeroBelowZ = ZERO_BELOW;
        private int groundTicks = 1;
        private @Nullable Integer maxAirTicks;
        private boolean entityPush = true;
        private boolean fluidPhysics = true;
        private boolean climbPhysics = true;
        private boolean webPhysics = true;
        private boolean flowPush = true;
        private FluidFlow.Model flowModel = FluidFlow.Model.LEGACY; // Vanilla(26) sets MODERN
        private boolean flowLava = true;    // MODERN only; Hypixel sets false
        private ClimbModel climbModel = ClimbModel.LEGACY; // Vanilla(26) sets MODERN
        private boolean modernBlockPhysics = false; // Vanilla(26) sets true
        private boolean motYOnMovePacket = false;   // 1.8-faithful presets set true; off = the 1.9+ server-tick law
        private @Nullable Double wireFloorX;
        private @Nullable Double wireFloorY;        // mmc18 sets 0.05
        private @Nullable Double wireFloorZ;

        Builder() {}

        Builder(VelocityConfig c) {
            seed = c.seed;
            launchOffset = c.launchOffset;
            zeroBelowX = c.zeroBelowX;
            zeroBelowY = c.zeroBelowY;
            zeroBelowZ = c.zeroBelowZ;
            groundTicks = c.groundTicks;
            maxAirTicks = c.maxAirTicks;
            entityPush = c.entityPush;
            fluidPhysics = c.fluidPhysics;
            climbPhysics = c.climbPhysics;
            webPhysics = c.webPhysics;
            flowPush = c.flowPush;
            flowModel = c.flowModel;
            flowLava = c.flowLava;
            climbModel = c.climbModel;
            modernBlockPhysics = c.modernBlockPhysics;
            motYOnMovePacket = c.motYOnMovePacket;
            wireFloorX = c.wireFloorX;
            wireFloorY = c.wireFloorY;
            wireFloorZ = c.wireFloorZ;
        }

        public Builder seed(double v) { seed = v; return this; }
        public Builder launchOffset(int v) { launchOffset = v; return this; }
        public Builder zeroBelow(double all) { zeroBelowX = all; zeroBelowY = all; zeroBelowZ = all; return this; }
        public Builder zeroBelowX(double v) { zeroBelowX = v; return this; }
        public Builder zeroBelowY(double v) { zeroBelowY = v; return this; }
        public Builder zeroBelowZ(double v) { zeroBelowZ = v; return this; }
        public Builder groundTicks(int v) { groundTicks = v; return this; }
        public Builder maxAirTicks(@Nullable Integer v) { maxAirTicks = v; return this; }
        public Builder entityPush(boolean v) { entityPush = v; return this; }
        public Builder fluidPhysics(boolean v) { fluidPhysics = v; return this; }
        public Builder climbPhysics(boolean v) { climbPhysics = v; return this; }
        public Builder webPhysics(boolean v) { webPhysics = v; return this; }
        public Builder flowPush(boolean v) { flowPush = v; return this; }
        public Builder flowModel(FluidFlow.Model v) { flowModel = v; return this; }
        public Builder flowLava(boolean v) { flowLava = v; return this; }
        public Builder climbModel(ClimbModel v) { climbModel = v; return this; }
        public Builder modernBlockPhysics(boolean v) { modernBlockPhysics = v; return this; }
        public Builder motYOnMovePacket(boolean v) { motYOnMovePacket = v; return this; }
        public Builder wireFloorX(@Nullable Double v) { wireFloorX = v; return this; }
        public Builder wireFloorY(@Nullable Double v) { wireFloorY = v; return this; }
        public Builder wireFloorZ(@Nullable Double v) { wireFloorZ = v; return this; }

        // the attacker self-slowdown lives on AttackConfig.fullHitScale (an attack-time mutation), not here

        public VelocityConfig build() {
            return new VelocityConfig(seed, launchOffset,
                    zeroBelowX, zeroBelowY, zeroBelowZ, groundTicks, maxAirTicks, entityPush, fluidPhysics, climbPhysics, webPhysics, flowPush, flowModel, flowLava, climbModel, modernBlockPhysics, motYOnMovePacket, wireFloorX, wireFloorY, wireFloorZ);
        }
    }
}
