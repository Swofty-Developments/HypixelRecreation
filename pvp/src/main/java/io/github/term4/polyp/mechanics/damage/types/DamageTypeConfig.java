package io.github.term4.polyp.mechanics.damage.types;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.config.TypeConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Per-type damage config: the common tunables shared by every {@link DamageType}, keyed by {@link #key()} and attached
 * to the global {@link DamageConfig} ({@code typeConfigs}); a type without an entry uses its {@link DamageType#defaultConfig()}.
 * Every value is a {@link FieldValue} (constant or context-aware), resolving to {@code null} to inherit the global config.
 */
@GenerateBuilder
public class DamageTypeConfig extends TypeConfig<DamageContext, DamageTypeConfig> {

    public final @Nullable FieldValue<DamageContext, Boolean> enabled;
    public final @Nullable FieldValue<DamageContext, Double> baseAmount;
    public final @Nullable FieldValue<DamageContext, Integer> invulTicks;
    public final @Nullable FieldValue<DamageContext, Boolean> overdamage;
    public final @Nullable FieldValue<DamageContext, Boolean> silent;
    public final @Nullable FieldValue<DamageContext, Boolean> overdamageSilent;
    public final @Nullable FieldValue<DamageContext, Boolean> triggersInvul;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassInvul;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassImmune;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassArmor;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassEffects;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassEnchants;
    public final @Nullable FieldValue<DamageContext, Boolean> bypassAll;
    public final @Nullable FieldValue<DamageContext, Boolean> ownsVelocityBroadcast;
    public final @Nullable FieldValue<DamageContext, Float> exhaustion;

    protected DamageTypeConfig(Builder b) {
        super(b.key, b.subConfig);
        this.enabled = b.enabled;
        this.baseAmount = b.baseAmount;
        this.invulTicks = b.invulTicks;
        this.overdamage = b.overdamage;
        this.silent = b.silent;
        this.overdamageSilent = b.overdamageSilent;
        this.triggersInvul = b.triggersInvul;
        this.bypassInvul = b.bypassInvul;
        this.bypassImmune = b.bypassImmune;
        this.bypassArmor = b.bypassArmor;
        this.bypassEffects = b.bypassEffects;
        this.bypassEnchants = b.bypassEnchants;
        this.bypassAll = b.bypassAll;
        this.ownsVelocityBroadcast = b.ownsVelocityBroadcast;
        this.exhaustion = b.exhaustion;
    }

    /** Whether this type applies in the resolved scope (default {@code true}); resolved per victim through the config chain. */
    public boolean enabled(DamageContext ctx) {
        Boolean v = resolve(enabled, ctx);
        return v == null || v;
    }

    /** Default damage amount for this context, or {@code null} (treated as 0) when a snapshot doesn't override it. */
    public @Nullable Double baseAmount(DamageContext ctx) { return resolve(baseAmount, ctx); }

    /** Per-type invulnerability window in ticks, or {@code null} to inherit {@link DamageConfig#invulTicks}. */
    public @Nullable Integer invulTicks(DamageContext ctx) { return resolve(invulTicks, ctx); }

    /** Constant per-type invul window when configured as a constant, else {@code null} (unset or context-dependent). */
    public @Nullable Integer invulTicksConstant() { return invulTicks != null ? invulTicks.constantOrNull() : null; }

    /** Per-type overdamage (damage-replacement), or {@code null} to inherit {@link DamageConfig#enableOverdamage}. */
    public @Nullable Boolean overdamage(DamageContext ctx) { return resolve(overdamage, ctx); }

    /** Per-type silent-damage (no hurt animation) for all hits, or {@code null} to inherit {@link DamageConfig#silent}. */
    public @Nullable Boolean silent(DamageContext ctx) { return resolve(silent, ctx); }

    /** Per-type silent-damage applied only to overdamage replacement hits, or {@code null} to inherit {@link DamageConfig#overdamageSilent}. */
    public @Nullable Boolean overdamageSilent(DamageContext ctx) { return resolve(overdamageSilent, ctx); }

    /** Whether applying this type starts the damage invulnerability window (defaults to {@code true}). */
    public boolean triggersInvul(DamageContext ctx) {
        Boolean v = resolve(triggersInvul, ctx);
        return v == null || v;
    }

    /** Whether this type ignores the target's i-frame window (vanilla {@code BYPASSES_COOLDOWN}). Default {@code false}. */
    public boolean bypassInvul(DamageContext ctx) {
        Boolean v = resolve(bypassInvul, ctx);
        return v != null && v;
    }

    /** Whether this type ignores fundamental immunity - creative/spectator (e.g. void / admin kill). Default {@code false}. */
    public boolean bypassImmune(DamageContext ctx) {
        Boolean v = resolve(bypassImmune, ctx);
        return v != null && v;
    }

    /** Whether this type skips the armor stage (vanilla {@code ignoresArmor} / {@code BYPASSES_ARMOR}: fall/drown/starve/suffocation/fire-tick/void/magic/wither). Default {@code false}. */
    public boolean bypassArmor(DamageContext ctx) {
        Boolean v = resolve(bypassArmor, ctx);
        return v != null && v;
    }

    /** Whether this type skips the resistance (effect) mitigation stage - "true damage" toward potion resistance. Default {@code false}. */
    public boolean bypassEffects(DamageContext ctx) {
        Boolean v = resolve(bypassEffects, ctx);
        return v != null && v;
    }

    /** Whether this type skips the EPF/Protection (enchant) mitigation stage - "true damage" toward armor enchants. Default {@code false}. */
    public boolean bypassEnchants(DamageContext ctx) {
        Boolean v = resolve(bypassEnchants, ctx);
        return v != null && v;
    }

    /** Whether this type skips every mitigation stage (armor + resistance + EPF) - the blanket bypass (e.g. void). Default {@code false}. */
    public boolean bypassAll(DamageContext ctx) {
        Boolean v = resolve(bypassAll, ctx);
        return v != null && v;
    }

    /** Whether this type's knockback owns the hurt-velocity broadcast, so {@code DamageSystem} won't also send the generic one. {@code null} = the built-in default (melee + thrown). */
    public @Nullable Boolean ownsVelocityBroadcast(DamageContext ctx) { return resolve(ownsVelocityBroadcast, ctx); }

    /** Exhaustion charged to a player victim per damaging hit (1.8: 0.3 or 0 for armor-bypass; modern: the per-type registry value). Unset = none. */
    public float exhaustion(DamageContext ctx) {
        Float v = resolve(exhaustion, ctx);
        return v != null ? v : 0f;
    }

    /**
     * Merges this type config over {@code base}: this config's set fields win, unset fields fall back
     * to {@code base} per resolution. Subclasses override to also merge their type-specific fields.
     */
    public DamageTypeConfig fromBase(DamageTypeConfig base) {
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        b.key = key() != null ? key() : base.key();
        b.subConfig = subConfig != null ? subConfig : base.subConfig;
        return b.build();
    }

    /** A builder for the base config; a key is required since configs are keyed by type. */
    public static Builder builder(Key key) { return new Builder().key(key); }

    /** Plain builder for the common knobs. Subclass builders compose one of these and delegate to it. */
    public static class Builder extends DamageTypeConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Key key;
        private Function<DamageContext, DamageTypeConfig> subConfig;

        public Builder key(Key key) { this.key = key; return this; }

        /** Copies every common field (and subConfig/key) from {@code src} into this builder. */
        public Builder copyFrom(DamageTypeConfig src) {
            copyKnobs(src);
            this.key = src.key();
            this.subConfig = src.subConfig;
            return this;
        }

        public Builder subConfig(Function<DamageContext, DamageTypeConfig> fn) { subConfig = fn; return this; }

        public DamageTypeConfig build() { return new DamageTypeConfig(this); }
    }
}
