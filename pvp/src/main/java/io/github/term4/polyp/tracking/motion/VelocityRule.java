package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Strategy for estimating an entity's velocity (b/t) for the knockback friction term. The base rule is
 * {@link #simulated(VelocityConfig) simulated}: the reconstructed server-tracked velocity (vanilla
 * {@code motX/motY/motZ}) - vertical from {@link MotionTracker}'s motY sim, horizontal from its impulse residual +
 * entity push + fluid flow. Configured per preset via {@link VelocityConfig}; {@link #split} mixes two rules per axis.
 * Functional, so a from-scratch lambda works.
 */
@FunctionalInterface
public interface VelocityRule {

    /** Estimated velocity (b/t) for the knockback friction term. */
    Vec estimate(VelocityContext ctx);

    /**
     * The reconstruction toggles {@link MotionTracker} reads off this rule. {@code null} = gate defaults (media
     * handling on, flow off). The seam that makes a custom rule first-class; arc knobs live in {@code arc}.
     */
    default @Nullable VelocityConfig reconstructionConfig() { return null; }

    VelocityRule DEFAULT = simulated();

    static VelocityRule simulated() { return simulated(VelocityConfig.defaults()); }

    static VelocityRule simulated(VelocityConfig cfg) { return new Simulated(cfg); }

    /** Exposes its {@link VelocityConfig} so {@link MotionTracker} can read the toggles. */
    record Simulated(VelocityConfig config) implements VelocityRule {
        @Override public Vec estimate(VelocityContext ctx) { return arc(ctx, config); }
        @Override public VelocityConfig reconstructionConfig() { return config; }
    }

    private static @Nullable VelocityConfig configOf(@Nullable VelocityRule rule) {
        return rule == null ? null : rule.reconstructionConfig();
    }

    /** On without a config. */
    static boolean fluidPhysicsEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c == null || c.fluidPhysics();
    }

    /** On without a config. */
    static boolean climbPhysicsEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c == null || c.climbPhysics();
    }

    /** On without a config. */
    static boolean webPhysicsEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c == null || c.webPhysics();
    }

    /** Off without a config: a from-scratch rule never inherits the flow residual. */
    static boolean flowPushEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null && c.flowPush();
    }

    /** On without a config: a config-less rule reads it through the context. */
    static boolean entityPushEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c == null || c.entityPush();
    }

    /** {@code LEGACY} without a config. */
    static FluidFlow.Model flowModel(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null ? c.flowModel() : FluidFlow.Model.LEGACY;
    }

    /** {@code LEGACY} without a config. Read once per tick. */
    static ClimbModel climbModel(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null ? c.climbModel() : ClimbModel.LEGACY;
    }

    /** Off (1.8) without a config. */
    static boolean modernBlockPhysicsEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null && c.modernBlockPhysics();
    }

    /** Off without a config. */
    static boolean flowLavaEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null && c.flowLava();
    }

    /** Off without a config. */
    static boolean motYOnMovePacketEnabled(@Nullable VelocityRule rule) {
        VelocityConfig c = configOf(rule);
        return c != null && c.motYOnMovePacket();
    }

    /** Per-context knobs (e.g. a ping-scaled {@code groundTicks}); use over a config lambda when only arc knobs vary. */
    static VelocityRule simulated(Function<VelocityContext, VelocityConfig> cfg) {
        return ctx -> arc(ctx, cfg.apply(ctx));
    }

    /**
     * Mixes two rules per axis: x/z from {@code horizontal}, y from {@code vertical}. A record (not a lambda) so the
     * reconstruction gates read their toggles off the {@code vertical} component (the one driving the motY sim).
     */
    record Split(VelocityRule horizontal, VelocityRule vertical) implements VelocityRule {
        @Override public Vec estimate(VelocityContext ctx) {
            Vec h = horizontal.estimate(ctx);
            return new Vec(h.x(), vertical.estimate(ctx).y(), h.z());
        }
        @Override public @Nullable VelocityConfig reconstructionConfig() { return vertical.reconstructionConfig(); }
    }

    static VelocityRule split(VelocityRule horizontal, VelocityRule vertical) {
        return new Split(horizontal, vertical);
    }

    /**
     * The server-tracked velocity fold (vanilla {@code motX/motY/motZ}): horizontal is the friction-bled sprint-jump
     * residual, not the knockback, plus the entity-push and flow residuals when enabled.
     */
    private static Vec arc(VelocityContext ctx, VelocityConfig cfg) {
        // non-players are server-simulated already
        if (!(ctx.entity() instanceof Player)) {
            return clamp(ctx.positionDelta(), cfg.clampX(), cfg.clampY(), cfg.clampZ());
        }
        Vec hMot = MotionTracker.horizontalMot(ctx.entity(), cfg.launchOffset());
        Vec out = new Vec(hMot.x(), verticalMot(ctx, cfg), hMot.z());
        if (cfg.entityPush()) {
            Vec push = MotionTracker.entityPush(ctx.entity());
            out = out.add(push.x(), 0, push.z());
        }
        if (cfg.flowPush()) {
            Vec flow = MotionTracker.flowPush(ctx.entity()); // 3D: x/z current + the Y down-term
            out = out.add(flow.x(), flow.y(), flow.z());
        }
        // wall-pinned mot reads 0 on the blocked axis (vanilla move() zeroing, measured)
        out = MotionTracker.zeroBlockedAxes(ctx.entity(), out);
        return clamp(out, cfg.clampX(), cfg.clampY(), cfg.clampZ());
    }

    /** The live ticked sim, falling back to the air clock before it has ticked. */
    private static double verticalMot(VelocityContext ctx, VelocityConfig cfg) {
        Double simY = MotionTracker.serverMotY(ctx.entity(),
                VelocityConfig.DEFAULT_LAUNCH_OFFSET - cfg.launchOffset(), cfg.clampY() > 0);
        return simY != null ? simY : reconstructedVy(ctx, cfg);
    }

    /** Seeds at launch and steps the air ticks, gated on ground state. */
    private static double reconstructedVy(VelocityContext ctx, VelocityConfig cfg) {
        boolean grounded = ctx.onGround(cfg.groundTicks());
        boolean launched = !grounded && ctx.launched();
        int air = grounded ? 0 : ctx.ticksInAir();
        if (cfg.maxAirTicks() != null) air = Math.min(air, cfg.maxAirTicks());
        int ticks = launched ? air + cfg.launchOffset() : air + 1;
        double seedY = launched ? cfg.seed() : 0;
        // the entity's OWN airborne motion, so it steps at the entity's dilated rate
        return steppedVy(ctx.entity(), TickScaler.aerodynamics(ctx.entity(), ctx.entity().getAerodynamics()),
                cfg.clampY(), seedY, ticks);
    }

    /** Apex-reseeds below {@code clampY} each step. */
    private static double steppedVy(Entity entity, Aerodynamics aero, double clampY, double seedY, int ticks) {
        if (ticks <= 0) return seedY;
        Vec vel = new Vec(0, seedY, 0);
        var pos = entity.getPosition();
        var blocks = entity.getInstance();
        for (int t = 0; t < ticks; t++) {
            if (Math.abs(vel.y()) < clampY) vel = vel.withY(0);
            vel = PhysicsUtils.updateVelocity(pos, vel, blocks, aero, true, false, false, false);
        }
        return vel.y();
    }

    private static Vec clamp(Vec v, double cx, double cy, double cz) {
        return new Vec(
                Math.abs(v.x()) < cx ? 0.0 : v.x(),
                Math.abs(v.y()) < cy ? 0.0 : v.y(),
                Math.abs(v.z()) < cz ? 0.0 : v.z());
    }
}
