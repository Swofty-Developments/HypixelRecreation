package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;

/** Vanilla 1.8 knockback configs; also the {@code KnockbackSystem} / {@code DamageSystem} fallbacks. */
public final class Knockback {

    private Knockback() {}

    /**
     * Every vanilla 1.8 value except {@code velocity}, left unset so the friction fold reads the scoped rule
     * ({@code MechanicsProfile.velocity(...)}). TODO(modern): 1.20+ KB differs - grounded-only vertical fold, vertical
     * add = power (not 0.4), resistance scales instead of gating, 0.003 clamp + apex micro-step.
     */
    public static KnockbackConfig melee() {
        return KnockbackConfig.builder()
                .sprintBuffer(0)
                .horizontal(0.4)
                .vertical(0.4)
                .extraHorizontal(0.5)
                .extraVertical(0.1)
                .verticalBounds(null, 0.4000000059604645)
                .yawWeight(0.0)
                .extraYawWeight(1.0)
                .pitchWeight(0.0)
                .extraPitchWeight(0.0)
                .heightDelta(0.0)
                .extraHeightDelta(0.0)
                .horizontalCombine(KnockbackConfig.DirectionMode.VECTOR_ADDITION)
                .verticalCombine(KnockbackConfig.DirectionMode.SCALAR)
                .frictionH(2.0)
                .frictionV(2.0)
                .frictionModeH(KnockbackConfig.FrictionMode.DIVISOR)
                .frictionModeV(KnockbackConfig.FrictionMode.DIVISOR)
                .quantizeVelocity(true)
                .build();
    }

    /**
     * In 1.8 a thrown projectile uses the thrower's {@link #melee() melee knockback} as a non-melee hit (no sprint
     * bonus). Punch is 0.6/level (EntityArrow), not melee Knockback's 0.5.
     */
    public static KnockbackConfig projectile() {
        return KnockbackConfig.builder(melee())
                .extraHorizontal(0.6)
                .build();
    }

    /**
     * Damage-tick "knockback" (fire, cactus, fall, ...): vanilla broadcasts the victim's server-tracked velocity with no
     * impulse, so all zeroes with a 1:1 friction fold - the velocity rule is the broadcast. Bounds cleared so the melee
     * {@code 0.4} vertical cap can't clip a jump's {@code 0.42} seed.
     */
    public static KnockbackConfig hurt() {
        return KnockbackConfig.builder()
                .sprintBuffer(0)
                .horizontal(0.0)
                .vertical(0.0)
                .extraHorizontal(0.0)
                .extraVertical(0.0)
                .horizontalBounds(KnockbackConfig.Bounds.of(null, null))
                .verticalBounds(KnockbackConfig.Bounds.of(null, null))
                .extraHorizontalBounds(KnockbackConfig.Bounds.of(null, null))
                .extraVerticalBounds(KnockbackConfig.Bounds.of(null, null))
                .yawWeight(0.0)
                .extraYawWeight(0.0)
                .pitchWeight(0.0)
                .extraPitchWeight(0.0)
                .heightDelta(0.0)
                .extraHeightDelta(0.0)
                .horizontalCombine(KnockbackConfig.DirectionMode.VECTOR_ADDITION)
                .verticalCombine(KnockbackConfig.DirectionMode.SCALAR)
                .frictionH(1.0)
                .frictionV(1.0)
                .frictionModeH(KnockbackConfig.FrictionMode.FACTOR)
                .frictionModeV(KnockbackConfig.FrictionMode.FACTOR)
                // velocity unset: the broadcast folds the scoped rule
                .quantizeVelocity(true)
                .build();
    }
}
