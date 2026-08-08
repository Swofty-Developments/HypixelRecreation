package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfigResolver.KnockbackContext;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.tracking.SprintTracker;
import io.github.term4.polyp.tracking.motion.VelocityConfig;
import io.github.term4.polyp.util.Directions;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * mmc18 melee knockback, reverse-engineered from MineMen captures. Horizontal magnitude is the product
 * {@code H = B(L) * axial * scale(d)} - the vanilla base pipeline supplies only the direction; see {@link #baseStrength},
 * {@link #axialFactor}, {@link #rangeReduction}. Vertical is the launch-hold then decay ({@link #verticalLaunchHold}).
 *
 * <p>The 1.8 per-axis +-3.9 wire cap is NOT applied here: it lives in {@code LegacyVelocity} (gated by {@code quantizeVelocity}).
 * The vertical fold reads {@code ctx.victimVelocity()} - the velocity rule inherited from {@code Vanilla18}.
 */
public final class Knockback {

    private Knockback() {}

    // gates the sprint bonus/enchant (attacker) and the axial factor (victim)
    static final int SPRINT_BUFFER = 5;

    public static KnockbackConfig melee() {
        return KnockbackConfig.builder(Vanilla18.knockback())
                .sprintBuffer(SPRINT_BUFFER)
                .horizontal(Knockback::baseStrength)
                .extraHorizontal(0.0)
                .vertical(VERTICAL_BASE)
                .extraVertical(0.0)
                .verticalBounds(0.050, VERTICAL_CAP)
                .yawWeight(0.5)
                .frictionH(0.0)
                .frictionV(VERTICAL_FRIC)
                .addCustomComponent(Knockback::verticalLaunchHold)
                .addCustomComponent(Knockback::axialFactor)
                .addCustomComponent(Knockback::rangeReduction)
                .build();
    }

    /**
     * MineMen projectile hit (bow/snowball/egg/pearl): the non-sprint melee profile - constant {@code B} horizontal (a
     * sprinting shooter gets no bonus), same vertical model - with Punch at +0.6/level (measured: 0.5274 -> 1.1274 ->
     * 1.7274) summed along the base's shooter-relative direction.
     */
    public static KnockbackConfig projectile() {
        return projectileBase()
                .addCustomComponent(Knockback::selfHitBlend)
                .build();
    }

    /** The bow's arrows: same non-sprint profile, but a SELF shot pushes 100% along the live look ({@link #selfHitLook}). */
    public static KnockbackConfig arrow() {
        return projectileBase()
                .addCustomComponent(Knockback::selfHitLook)
                .build();
    }

    private static KnockbackConfig.Builder projectileBase() {
        return KnockbackConfig.builder(melee())
                .horizontal(HORIZONTAL_BASE)
                .extraHorizontal(0.6)
                .extraYawWeight(0.0)
                .yawWeight(0.0);
    }

    /**
     * MineMen rod hit, SHOOTER-relative (1.8 {@code damageEntity} reads the indirect source, the angler): the old lib's
     * tuned values on the vanilla 1.8 fold. Pending capture confirmation.
     */
    public static KnockbackConfig rod() {
        return KnockbackConfig.builder(Vanilla18.knockback())
                .horizontal(0.525)
                .vertical(0.365)
                .verticalBounds(null, 0.45)
                .addCustomComponent(Knockback::selfHitBlend)
                .build();
    }

    /**
     * The hurt-KB an explosion folds before its radial push: the melee profile, direction purely positional away from
     * the damager. The melee-gated components self-disable on the non-melee snapshot.
     */
    public static KnockbackConfig explosionHurt() {
        return KnockbackConfig.builder(melee())
                .yawWeight(0.0)
                .addCustomComponent(Knockback::pointBlankFallback)
                .build();
    }

    // horizontal strength
    static final double HORIZONTAL_BASE = 0.5274;
    private static final double SPRINT_BONUS = 0.3271;
    private static final double ENCHANT_PER_LEVEL = 0.37979;

    // B(L) = base + sprint bonus + enchant, carried on the horizontal (x1) field: extraHorizontal x extraLevel is
    // uniform per level, so it couldn't give the sprint its own strength
    private static double baseStrength(KnockbackContext ctx) {
        Entity attacker = ctx.snap().source();
        double b = HORIZONTAL_BASE;
        if (attacker != null && recentlySprinting(ctx, attacker)) b += SPRINT_BONUS + ENCHANT_PER_LEVEL * ctx.snap().extraKnockback();
        return b;
    }

    // axial
    private static final double AXIAL = 1.0 / 9.0;

    // 1 -/+ 1/9 from the victim's facing vs the attacker's: fleeing -> 8/9, charging -> 10/9. The sprint gate means the
    // victim moves along their yaw, so yaw stands in for the motion direction - no reconstructed velocity needed
    @Nullable
    private static Vec axialFactor(KnockbackContext ctx, Vec kb) {
        var snap = ctx.snap();
        Entity attacker = snap.source();
        Entity target = snap.target();
        if (attacker == null || target == null || !snap.melee()) return null;
        if (!recentlySprinting(ctx, attacker) || !clientRecentlySprinting(ctx, target)) return null;
        Vec victimFacing = Directions.fromYaw(target.getPosition().yaw());
        Vec attackerFacing = Directions.fromYaw(attacker.getPosition().yaw());
        double proj = victimFacing.x() * attackerFacing.x() + victimFacing.z() * attackerFacing.z();   // cos(victim yaw - attacker yaw)
        if (proj == 0) return null;
        double a = proj > 0 ? 1.0 - AXIAL : 1.0 + AXIAL;
        return new Vec(kb.x() * a, kb.y(), kb.z() * a);
    }

    // range reduction
    private static final double RANGE_START = 3.03;
    private static final double RANGE_SLOPE = 0.47;
    private static final double RANGE_FLOOR = 2.0 / 3.0;

    // min(1, 1 - 0.47*(d - 3.03)) on horizontal, floored at 2/3 * B(L); the floor is on the FINAL magnitude and
    // independent of the axial factor
    @Nullable
    private static Vec rangeReduction(KnockbackContext ctx, Vec kb) {
        var snap = ctx.snap();
        Entity attacker = snap.source();
        Entity target = snap.target();
        if (attacker == null || target == null || !snap.melee() || !recentlySprinting(ctx, attacker)) return null;
        var a = attacker.getPosition();
        var t = target.getPosition();
        double dist = Math.hypot(t.x() - a.x(), t.z() - a.z());
        double scale = Math.min(1.0, 1.0 - RANGE_SLOPE * (dist - RANGE_START));
        double floor = RANGE_FLOOR * baseStrength(ctx);
        double preH = Math.hypot(kb.x(), kb.z());
        double postH = Math.max(floor, preH * scale);
        if (postH >= preH) return null;
        double s = postH / preH;
        return new Vec(kb.x() * s, kb.y(), kb.z() * s);
    }

    // a queued Attack flush on a projectile-hit victim reads non-sprinting (measured: it lands at the plain base)
    private static boolean recentlySprinting(KnockbackContext ctx, Entity e) {
        if (Attack.flushingNonSprint(ctx.snap().target())) return false;
        return SprintTracker.wasRecentlySprinting(ctx.services().sprintTracker(),
                e, TickScaler.duration(e, SPRINT_BUFFER, KnockbackSystem.KEY));
    }

    // MineMen's fixed yaw-135 stand-in wherever the relative position is meaningless (point-blank blasts, self-hits)
    private static final Vec DEGENERATE_DIAGONAL = new Vec(-1, 0, -1).normalize();

    // damager horizontally on top of the victim (vanilla's d0*d0+d1*d1 < 1e-4) -> the fixed diagonal at full B, not
    // vanilla's random pick; verified yaw/position-independent, always (-2983,-2983) shorts
    @Nullable
    private static Vec pointBlankFallback(KnockbackContext ctx, Vec kb) {
        var snap = ctx.snap();
        Entity source = snap.source();
        Entity target = snap.target();
        if (source == null || target == null) return null;
        double dx = target.getPosition().x() - source.getPosition().x();
        double dz = target.getPosition().z() - source.getPosition().z();
        if (dx * dx + dz * dz >= 1.0e-4) return null;
        return new Vec(DEGENERATE_DIAGONAL.x() * HORIZONTAL_BASE, kb.y(), DEGENERATE_DIAGONAL.z() * HORIZONTAL_BASE);
    }

    // Punch is priced here, not by the pipeline: the extra folds BEFORE components run, so the horizontal replace
    // would wipe it
    private static double selfStrength(KnockbackContext ctx) {
        return HORIZONTAL_BASE + 0.6 * ctx.snap().extraKnockback();
    }

    // (B/2)(diagonal + look): the melee 50/50 position+look blend with the position half on the fixed diagonal.
    // Capture-solved (snowball+rod selfhit 2026-07-17, 30 hits): |h| = B*cos(delta/2) to +-0.002, vertical unchanged
    @Nullable
    private static Vec selfHitBlend(KnockbackContext ctx, Vec kb) {
        var snap = ctx.snap();
        Entity source = snap.source();
        if (source == null || source != snap.target()) return null;
        Vec look = Directions.fromYaw(source.getPosition().yaw());
        double h = selfStrength(ctx) / 2;
        return new Vec((DEGENERATE_DIAGONAL.x() + look.x()) * h, kb.y(), (DEGENERATE_DIAGONAL.z() + look.z()) * h);
    }

    // full strength 100% along the live look (user-verified on MineMen)
    @Nullable
    private static Vec selfHitLook(KnockbackContext ctx, Vec kb) {
        var snap = ctx.snap();
        Entity source = snap.source();
        if (source == null || source != snap.target()) return null;
        Vec look = Directions.fromYaw(source.getPosition().yaw());
        double h = selfStrength(ctx);
        return new Vec(look.x() * h, kb.y(), look.z() * h);
    }

    // what the client reports, even if the server disagrees
    private static boolean clientRecentlySprinting(KnockbackContext ctx, Entity e) {
        return SprintTracker.wasClientRecentlySprinting(ctx.services().sprintTracker(),
                e, TickScaler.duration(e, SPRINT_BUFFER, KnockbackSystem.KEY));
    }

    private static final int VERTICAL_DECAY_N = 7;
    private static final double VERTICAL_CAP = 0.3614;
    private static final double VERTICAL_BASE = VERTICAL_CAP + VelocityConfig.GRAVITY / VERTICAL_DECAY_N;  // 0.3728286
    private static final double VERTICAL_FRIC = VERTICAL_DECAY_N * VelocityConfig.DRAG_V;                  // 6.86
    private static final double VERTICAL_HOLD_RELEASE = -VelocityConfig.JUMP_VELOCITY;                     // -0.42

    // pins vertical to the cap through the launch arc; frictionV handles the decay below the release threshold
    @Nullable
    private static Vec verticalLaunchHold(KnockbackContext ctx, Vec kb) {
        return ctx.victimVelocity().y() <= VERTICAL_HOLD_RELEASE ? null : new Vec(kb.x(), VERTICAL_CAP, kb.z());
    }
}
