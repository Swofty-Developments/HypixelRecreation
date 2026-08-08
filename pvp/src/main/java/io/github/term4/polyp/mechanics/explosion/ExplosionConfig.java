package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfigResolver.ExplosionContext;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Immutable per-explosion config (radius, damage curve, knockback, exposure). Mitigation/i-frames/death are the
 * {@code DamageSystem}'s, not configured here. Use {@link #builder()}, {@link #toBuilder()}.
 *
 * <p>Two knockback paths: {@link #baseKnockback} &gt; 0 = a radial base toward {@code feet+baseHeight} plus the push,
 * SET as one velocity (Hypixel); {@code 0} = the vanilla hurt-KB fold ({@link #damageKnockback}) plus the push.
 */
@GenerateBuilder
public final class ExplosionConfig extends Config<ExplosionContext, ExplosionConfig> {

    /** Which cells the incendiary pass may light. */
    public enum FireScope {
        /** Every selected cell (vanilla) - fire can land on intact surfaces the blast merely reached. */
        SELECTED,
        /** Only cells a block was actually broken in (Hypixel fireballs) - a blast on unbroken ground lights nothing. */
        BROKEN
    }

    /** Default radius for the no-power {@code explode} overloads; an explicit call power wins. */
    public final FieldValue<ExplosionContext, Double> power;
    /** Damage-curve constant: 8.0 (1.8) or 7.0 (modern). */
    public final FieldValue<ExplosionContext, Double> damageConstant;
    /** Floor the per-entity damage to an int (1.8 parity). */
    public final FieldValue<ExplosionContext, Boolean> floorDamage;
    /** Flat damage to every in-range target, overriding the falloff curve (Hypixel/BedWars = 2.0). {@code null} = use the curve. */
    public final FieldValue<ExplosionContext, Double> flatDamage;
    /** Damage this blast deals to a DROPPED ITEM; unset = the same curve amount a player takes. */
    public final FieldValue<ExplosionContext, Double> itemDamage;
    /** Scale on the final damage, applied AFTER the floor (MineMen Fireball-Fight = the vanilla floored curve × 0.05). Default 1.0. */
    public final FieldValue<ExplosionContext, Double> damageScale;
    /** Mitigation the explosion damage skips (e.g. armor points only); {@code null} = normal mitigation. */
    public final @Nullable Bypass damageBypass;
    /** What this explosion does to blocks; {@code null} = breaks none. Resolved per blast, so one config can swap the
     *  whole policy by source (MineMen: fireball = radius ball, TNT = rays). */
    public final FieldValue<ExplosionContext, BlockBreaking> blockBreaking;
    /** Scale on the radial falloff push ({@code impact · multiplier}); vanilla 1.0. */
    public final FieldValue<ExplosionContext, Double> knockbackMultiplier;
    /** Damage-knockback on a fresh hit (before the push); {@code null} = the vanilla 1.8 {@code a()}. Only used when {@link #baseKnockback} is 0. */
    public final @Nullable KnockbackConfig damageKnockback;
    /** Client-applied push on the explosion packet when the velocity path doesn't fire (default, vanilla); false = velocity-only, blocked hits get nothing (MineMen sends an all-zero packet). */
    public final FieldValue<ExplosionContext, Boolean> packetPush;
    /** Radial base magnitude, toward {@link #baseHeight} above the feet ({@link #baseScale} shapes the axes). 0 = use {@link #damageKnockback}. */
    public final FieldValue<ExplosionContext, Double> baseKnockback;
    /** Height above the feet the radial {@link #baseKnockback} aims at; default 1.0. */
    public final FieldValue<ExplosionContext, Double> baseHeight;
    /** Per-direction scaling of the base; {@code null} = isotropic. */
    public final @Nullable RadialScale baseScale;
    /** Line-of-sight exposure rays (default {@code MODERN}); 1.8 rays differ at block-edge shadows, {@code NONE} = full exposure. */
    public final FieldValue<ExplosionContext, ExplosionExposure.Rays> exposure;
    /** Hypixel KB gate: below this falloff {@link ExplosionCalculator#impact impact}, no explosion KB (only the projectile KB). {@code null} = no gate; Hypixel ≈ 0.435. */
    public final FieldValue<ExplosionContext, Double> knockbackImpactFloor;
    /** Carried on {@code ExplosionEvent} for a block/fire listener; the library itself never sets fire. */
    public final FieldValue<ExplosionContext, Boolean> fire;
    /** Where {@link #fire} lands; {@code null} = {@link FireScope#SELECTED} (vanilla). */
    public final @Nullable FireScope fireScope;
    /** Whether the source entity is hit by its own explosion (vanilla excludes it). */
    public final FieldValue<ExplosionContext, Boolean> affectsSource;
    /** Which entities the explosion knocks back. {@code null} = players + non-living physics entities (TNT etc.); e.g. {@code e -> e instanceof Player} restores player-only. */
    public final @Nullable Predicate<Entity> knockbackTargets;
    /** Per-victim eye height for the push direction (living entities only); {@code null} = the standing registry eye (Hypixel pushes from 1.62 even sneaking; vanilla/MineMen use the sneak-aware head height). */
    public final @Nullable Function<Entity, Double> pushEye;

    private ExplosionConfig(Builder b) {
        super(b.subConfig);
        power = b.power;
        damageConstant = b.damageConstant;
        floorDamage = b.floorDamage;
        flatDamage = b.flatDamage;
        itemDamage = b.itemDamage;
        damageScale = b.damageScale;
        damageBypass = b.damageBypass;
        blockBreaking = b.blockBreaking;
        knockbackMultiplier = b.knockbackMultiplier;
        damageKnockback = b.damageKnockback;
        packetPush = b.packetPush;
        baseKnockback = b.baseKnockback;
        baseHeight = b.baseHeight;
        baseScale = b.baseScale;
        exposure = b.exposure;
        knockbackImpactFloor = b.knockbackImpactFloor;
        fire = b.fire;
        fireScope = b.fireScope;
        affectsSource = b.affectsSource;
        knockbackTargets = b.knockbackTargets;
        pushEye = b.pushEye;
    }

    /** Merges this config over base. */
    public ExplosionConfig fromBase(ExplosionConfig base) {
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        return b
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .damageBypass(damageBypass != null ? damageBypass : base.damageBypass)
                .baseScale(baseScale != null ? baseScale : base.baseScale)
                .fireScope(fireScope != null ? fireScope : base.fireScope)
                .damageKnockback(damageKnockback != null ? damageKnockback : base.damageKnockback)
                .knockbackTargets(knockbackTargets != null ? knockbackTargets : base.knockbackTargets)
                .pushEye(pushEye != null ? pushEye : base.pushEye)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return new Builder(); }

    public static Builder builder(@Nullable ExplosionConfig base) {
        return base != null ? new Builder(base) : new Builder();
    }

    public static final class Builder extends ExplosionConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<ExplosionContext, ExplosionConfig> subConfig;
        private Bypass damageBypass;
        private RadialScale baseScale;
        private FireScope fireScope;
        private KnockbackConfig damageKnockback;
        private Predicate<Entity> knockbackTargets;
        private Function<Entity, Double> pushEye;

        Builder() {}

        Builder(ExplosionConfig c) {
            super(c);
            subConfig = c.subConfig;
            damageBypass = c.damageBypass;
            baseScale = c.baseScale;
            fireScope = c.fireScope;
            damageKnockback = c.damageKnockback;
            knockbackTargets = c.knockbackTargets;
            pushEye = c.pushEye;
        }

        public Builder subConfig(Function<ExplosionContext, ExplosionConfig> fn) { subConfig = fn; return this; }
        public Builder damageBypass(Bypass v) { damageBypass = v; return this; }
        public Builder baseScale(RadialScale v) { baseScale = v; return this; }
        public Builder fireScope(FireScope v) { fireScope = v; return this; }
        public Builder damageKnockback(KnockbackConfig v) { damageKnockback = v; return this; }
        public Builder knockbackTargets(Predicate<Entity> v) { knockbackTargets = v; return this; }
        public Builder pushEye(Function<Entity, Double> v) { pushEye = v; return this; }

        public ExplosionConfig build() { return new ExplosionConfig(this); }
    }
}
