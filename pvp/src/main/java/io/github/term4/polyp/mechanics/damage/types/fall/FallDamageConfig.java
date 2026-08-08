package io.github.term4.polyp.mechanics.damage.types.fall;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Config for {@link FallDamage}. Formula knobs are first-class fields; vanilla values are wired in {@code Vanilla18}/{@code Vanilla}.
 * Override {@code baseAmount} with a lambda for fully custom behavior.
 */
@GenerateBuilder
public final class FallDamageConfig extends DamageTypeConfig {

    /** Which vanilla fall-damage formula to apply when {@code baseAmount} is not explicitly set. */
    public enum Formula {
        /** 1.8: {@code ceil(distance - threshold)}. */
        LEGACY_CEIL,
        /** 26.1+: {@code floor((distance + ε - threshold) * damageModifier * fallDamageMultiplier)}. */
        MODERN_FLOOR
    }

    private static final double MODERN_EPSILON = 1.0E-6;

    public final @Nullable FieldValue<DamageContext, Formula> formula;
    public final @Nullable FieldValue<DamageContext, Double> threshold;
    public final @Nullable FieldValue<DamageContext, Double> damageModifier;
    public final @Nullable FieldValue<DamageContext, Double> fallDamageMultiplier;

    private FallDamageConfig(Builder b) {
        super(b.common);
        this.formula = b.formula;
        this.threshold = b.threshold;
        this.damageModifier = b.damageModifier;
        this.fallDamageMultiplier = b.fallDamageMultiplier;
    }

    public @Nullable Formula formula(DamageContext ctx) { return resolve(formula, ctx); }

    /** Safe fall distance in blocks (maps to 1.8 threshold / modern {@code SAFE_FALL_DISTANCE}). */
    public @Nullable Double threshold(DamageContext ctx) { return resolve(threshold, ctx); }

    /** Per-landing damage modifier (modern {@code causeFallDamage}); a {@link FallDetail#damageModifier()} on the snapshot wins. */
    public @Nullable Double damageModifier(DamageContext ctx) { return resolve(damageModifier, ctx); }

    /** Modern {@code FALL_DAMAGE_MULTIPLIER} attribute (ignored by {@link Formula#LEGACY_CEIL}). */
    public @Nullable Double fallDamageMultiplier(DamageContext ctx) { return resolve(fallDamageMultiplier, ctx); }

    /** Explicit {@code baseAmount} when set, else the configured {@link #formula} and knobs. */
    @Override
    public @Nullable Double baseAmount(DamageContext ctx) {
        Double explicit = super.baseAmount(ctx);
        return explicit != null ? explicit : computeAmount(ctx);
    }

    private double computeAmount(DamageContext ctx) {
        Float distance = fallDistance(ctx);
        if (distance == null || distance <= 0f) return 0.0;

        Double safeFall = threshold(ctx);
        double safe = safeFall != null ? safeFall : 3.0;
        Formula mode = formula(ctx);
        if (mode == null) mode = Formula.LEGACY_CEIL;

        return switch (mode) {
            case LEGACY_CEIL -> Math.max(0.0, Math.ceil(distance - safe));
            case MODERN_FLOOR -> {
                double mod = effectiveDamageModifier(ctx);
                Double multiplier = fallDamageMultiplier(ctx);
                double mult = multiplier != null ? multiplier : 1.0;
                double power = distance + MODERN_EPSILON - safe;
                yield Math.max(0.0, Math.floor(power * mod * mult));
            }
        };
    }

    private static @Nullable Float fallDistance(DamageContext ctx) {
        FallDetail detail = ctx.detail(FallDetail.class);
        if (detail != null) return detail.distance();
        return ctx.detail(Float.class);
    }

    private double effectiveDamageModifier(DamageContext ctx) {
        FallDetail detail = ctx.detail(FallDetail.class);
        if (detail != null && detail.damageModifier() != null) return detail.damageModifier();
        Double cfg = damageModifier(ctx);
        return cfg != null ? cfg : 1.0;
    }

    @Override
    public DamageTypeConfig fromBase(DamageTypeConfig base) {
        DamageTypeConfig mergedCommon = super.fromBase(base);
        Builder b = new Builder();
        b.common.copyFrom(mergedCommon);
        if (base instanceof FallDamageConfig f) b.mergeKnobs(this, f);
        else b.copyKnobs(this);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends FallDamageConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private final DamageTypeConfig.Builder common = new DamageTypeConfig.Builder().key(FallDamage.KEY);

        public Builder enabled(Boolean v) { common.enabled(v); return this; }
        public Builder enabled(Function<DamageContext, Boolean> fn) { common.enabled(fn); return this; }
        public Builder baseAmount(Double v) { common.baseAmount(v); return this; }
        public Builder baseAmount(Function<DamageContext, Double> fn) { common.baseAmount(fn); return this; }
        public Builder baseAmount(Double fallback, Function<DamageContext, Double> fn) { common.baseAmount(fallback, fn); return this; }
        public Builder invulTicks(Integer v) { common.invulTicks(v); return this; }
        public Builder invulTicks(Function<DamageContext, Integer> fn) { common.invulTicks(fn); return this; }
        public Builder invulTicks(Integer fallback, Function<DamageContext, Integer> fn) { common.invulTicks(fallback, fn); return this; }
        public Builder overdamage(Boolean v) { common.overdamage(v); return this; }
        public Builder overdamage(Function<DamageContext, Boolean> fn) { common.overdamage(fn); return this; }
        public Builder overdamage(Boolean fallback, Function<DamageContext, Boolean> fn) { common.overdamage(fallback, fn); return this; }
        public Builder silent(Boolean v) { common.silent(v); return this; }
        public Builder silent(Function<DamageContext, Boolean> fn) { common.silent(fn); return this; }
        public Builder silent(Boolean fallback, Function<DamageContext, Boolean> fn) { common.silent(fallback, fn); return this; }
        public Builder overdamageSilent(Boolean v) { common.overdamageSilent(v); return this; }
        public Builder overdamageSilent(Function<DamageContext, Boolean> fn) { common.overdamageSilent(fn); return this; }
        public Builder overdamageSilent(Boolean fallback, Function<DamageContext, Boolean> fn) { common.overdamageSilent(fallback, fn); return this; }
        public Builder triggersInvul(Boolean v) { common.triggersInvul(v); return this; }
        public Builder triggersInvul(Function<DamageContext, Boolean> fn) { common.triggersInvul(fn); return this; }
        public Builder bypassInvul(Boolean v) { common.bypassInvul(v); return this; }
        public Builder bypassInvul(Function<DamageContext, Boolean> fn) { common.bypassInvul(fn); return this; }
        public Builder bypassImmune(Boolean v) { common.bypassImmune(v); return this; }
        public Builder bypassImmune(Function<DamageContext, Boolean> fn) { common.bypassImmune(fn); return this; }
        public Builder bypassArmor(Boolean v) { common.bypassArmor(v); return this; }
        public Builder bypassArmor(Function<DamageContext, Boolean> fn) { common.bypassArmor(fn); return this; }
        public Builder bypassEffects(Boolean v) { common.bypassEffects(v); return this; }
        public Builder bypassEffects(Function<DamageContext, Boolean> fn) { common.bypassEffects(fn); return this; }
        public Builder bypassEnchants(Boolean v) { common.bypassEnchants(v); return this; }
        public Builder bypassEnchants(Function<DamageContext, Boolean> fn) { common.bypassEnchants(fn); return this; }
        public Builder bypassAll(Boolean v) { common.bypassAll(v); return this; }
        public Builder bypassAll(Function<DamageContext, Boolean> fn) { common.bypassAll(fn); return this; }
        public Builder ownsVelocityBroadcast(Boolean v) { common.ownsVelocityBroadcast(v); return this; }
        public Builder ownsVelocityBroadcast(Function<DamageContext, Boolean> fn) { common.ownsVelocityBroadcast(fn); return this; }
        public Builder subConfig(Function<DamageContext, DamageTypeConfig> fn) { common.subConfig(fn); return this; }


        public FallDamageConfig build() { return new FallDamageConfig(this); }
    }
}
