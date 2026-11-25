package io.github.term4.polyp.mechanics.damage.types.burning;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

// TODO: an API hook / config for what extinguishes burning (or whether it can be).
/**
 * Config for the burning family ({@link InFireDamage}, {@link LavaDamage}, {@link BurningDamage}), keyed per member.
 * Adds the family's scheduling knobs (ignite ticks, ignite warmup, contact/burn intervals, skip-burn-in-lava) on top of
 * the common {@link DamageTypeConfig} tunables; vanilla values are wired in {@code Vanilla18}/{@code Vanilla}.
 */
@GenerateBuilder
public final class BurningConfig extends DamageTypeConfig {

    public final @Nullable FieldValue<DamageContext, Integer> igniteTicks;
    public final @Nullable FieldValue<DamageContext, Integer> igniteWarmupTicks;
    public final @Nullable FieldValue<DamageContext, Integer> igniteWarmupInvulMult;
    public final @Nullable FieldValue<DamageContext, Integer> contactIntervalTicks;
    public final @Nullable FieldValue<DamageContext, Integer> intervalTicks;
    public final @Nullable FieldValue<DamageContext, Boolean> skipBurnWhileInLava;

    private BurningConfig(Builder b) {
        super(b.common);
        this.igniteTicks = b.igniteTicks;
        this.igniteWarmupTicks = b.igniteWarmupTicks;
        this.igniteWarmupInvulMult = b.igniteWarmupInvulMult;
        this.contactIntervalTicks = b.contactIntervalTicks;
        this.intervalTicks = b.intervalTicks;
        this.skipBurnWhileInLava = b.skipBurnWhileInLava;
    }

    /** Fire ticks pinned on contact (vanilla: in-fire 160, lava 300), or {@code null}/non-positive for none. */
    public @Nullable Integer igniteTicks(DamageContext ctx) { return resolve(igniteTicks, ctx); }

    /** Explicit contact ticks before first ignite; overrides {@link #igniteWarmupInvulMult} when set. */
    public @Nullable Integer igniteWarmupTicks(DamageContext ctx) { return resolve(igniteWarmupTicks, ctx); }

    /** Felt damage hits before first ignite when {@link #igniteWarmupTicks} is unset (1.8 = 2, modern = 3). */
    public @Nullable Integer igniteWarmupInvulMult(DamageContext ctx) { return resolve(igniteWarmupInvulMult, ctx); }

    /** Contact ticks before the first ignite: explicit {@link #igniteWarmupTicks}, else {@code (mult-1)*invulTicks+1}, else {@code 11}. */
    public int resolveIgniteWarmup(DamageContext ctx, @Nullable Integer invulTicks) {
        Integer explicit = igniteWarmupTicks(ctx);
        if (explicit != null && explicit > 0) return explicit;
        Integer mult = igniteWarmupInvulMult(ctx);
        if (mult != null && mult > 1 && invulTicks != null && invulTicks > 0) {
            return (mult - 1) * invulTicks + 1;
        }
        return 11;
    }

    /** Ticks between contact-damage attempts while in fire/lava (vanilla = 1). */
    public @Nullable Integer contactIntervalTicks(DamageContext ctx) { return resolve(contactIntervalTicks, ctx); }

    /** Ticks between successive burn hits while on fire (vanilla = 20). */
    public @Nullable Integer intervalTicks(DamageContext ctx) { return resolve(intervalTicks, ctx); }

    /** When true, {@code on_fire} damage is not applied while standing in lava (26.1). */
    public @Nullable Boolean skipBurnWhileInLava(DamageContext ctx) { return resolve(skipBurnWhileInLava, ctx); }

    @Override
    public DamageTypeConfig fromBase(DamageTypeConfig base) {
        DamageTypeConfig mergedCommon = super.fromBase(base);
        Builder b = new Builder();
        b.common.copyFrom(mergedCommon);
        if (base instanceof BurningConfig f) b.mergeKnobs(this, f);
        else b.copyKnobs(this);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends BurningConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private final DamageTypeConfig.Builder common = new DamageTypeConfig.Builder();

        public Builder key(Key key) { common.key(key); return this; }

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
        public Builder exhaustion(Float v) { common.exhaustion(v); return this; }
        public Builder exhaustion(Function<DamageContext, Float> fn) { common.exhaustion(fn); return this; }
        public Builder bypassEffects(Boolean v) { common.bypassEffects(v); return this; }
        public Builder bypassEffects(Function<DamageContext, Boolean> fn) { common.bypassEffects(fn); return this; }
        public Builder bypassEnchants(Boolean v) { common.bypassEnchants(v); return this; }
        public Builder bypassEnchants(Function<DamageContext, Boolean> fn) { common.bypassEnchants(fn); return this; }
        public Builder bypassAll(Boolean v) { common.bypassAll(v); return this; }
        public Builder bypassAll(Function<DamageContext, Boolean> fn) { common.bypassAll(fn); return this; }
        public Builder ownsVelocityBroadcast(Boolean v) { common.ownsVelocityBroadcast(v); return this; }
        public Builder ownsVelocityBroadcast(Function<DamageContext, Boolean> fn) { common.ownsVelocityBroadcast(fn); return this; }
        public Builder subConfig(Function<DamageContext, DamageTypeConfig> fn) { common.subConfig(fn); return this; }



        public BurningConfig build() { return new BurningConfig(this); }
    }
}
