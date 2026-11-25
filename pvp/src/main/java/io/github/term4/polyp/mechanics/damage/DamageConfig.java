package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Immutable damage config. Use {@link #builder()}, {@link #toBuilder()}. Mirrors KnockbackConfig. */
@GenerateBuilder
public final class DamageConfig extends Config<DamageContext, DamageConfig> {

    public final FieldValue<DamageContext, Integer> invulTicks;
    public final FieldValue<DamageContext, Boolean> enableOverdamage;
    public final FieldValue<DamageContext, Boolean> silent;
    public final FieldValue<DamageContext, Boolean> overdamageSilent;
    /** When true, fresh hits (except drowning) broadcast the victim's current velocity (vanilla {@code ac()}/{@code hurtMarked}). */
    public final FieldValue<DamageContext, Boolean> syncHurtVelocity;
    /**
     * The knockback config the {@link #syncHurtVelocity hurt broadcast} routes through the KnockbackSystem - a
     * zero-impulse config whose velocity fold is the broadcast (default {@code Vanilla18.hurtKb()}). Keeps one send path.
     */
    public final FieldValue<DamageContext, KnockbackConfig> hurtKnockback;

    /** Per-type config overrides, keyed by {@link DamageTypeConfig#key()}. Unset knobs fall back to this config. */
    public final Map<Key, DamageTypeConfig> typeConfigs;

    /**
     * Pluggable transforms applied in order after the {@link io.github.term4.polyp.api.event.damage.DamageEvent}
     * fires; each self-gates and may adjust the amount.
     * TODO(stages): per-stage strategy plan like knockback - let users replace a built-in formula, not just append.
     */
    @Nullable public final List<DamageComponent> customComponents;

    private DamageConfig(Builder b) {
        super(b.subConfig);
        invulTicks = b.invulTicks;
        enableOverdamage = b.enableOverdamage;
        silent = b.silent;
        overdamageSilent = b.overdamageSilent;
        syncHurtVelocity = b.syncHurtVelocity;
        hurtKnockback = b.hurtKnockback;
        typeConfigs = Map.copyOf(b.typeConfigs);
        customComponents = b.customComponents;
    }

    /** Per-type config for {@code key}, or {@code null} if none was registered. */
    public @Nullable DamageTypeConfig typeConfig(Key key) {
        return typeConfigs.get(key);
    }

    /** Merges this config over base. */
    public DamageConfig fromBase(DamageConfig base) {
        Map<Key, DamageTypeConfig> mergedTypes = new LinkedHashMap<>(base.typeConfigs);
        mergedTypes.putAll(typeConfigs);
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        return b
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .typeConfigs(mergedTypes)
                .customComponents(customComponents != null ? customComponents : base.customComponents)
                .build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(@Nullable DamageConfig base) {
        return base != null ? new Builder(base) : new Builder();
    }

    public static final class Builder extends DamageConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<DamageContext, DamageConfig> subConfig;
        private final Map<Key, DamageTypeConfig> typeConfigs = new LinkedHashMap<>();
        private List<DamageComponent> customComponents;

        Builder() {}

        Builder(DamageConfig c) {
            super(c);
            subConfig = c.subConfig;
            typeConfigs.putAll(c.typeConfigs);
            customComponents = c.customComponents;
        }

        public Builder subConfig(Function<DamageContext, DamageConfig> fn) { subConfig = fn; return this; }

        /** Adds a single per-type config override, keyed by its {@link DamageTypeConfig#key()}. */
        public Builder typeConfig(DamageTypeConfig cfg) { typeConfigs.put(cfg.key(), cfg); return this; }

        /** Adds per-type config overrides, each keyed by its {@link DamageTypeConfig#key()}. */
        public Builder typeConfigs(DamageTypeConfig... cfgs) {
            for (DamageTypeConfig cfg : cfgs) typeConfigs.put(cfg.key(), cfg);
            return this;
        }

        public Builder addCustomComponent(DamageComponent component) {
            List<DamageComponent> list = customComponents == null ? new ArrayList<>() : new ArrayList<>(customComponents);
            list.add(component);
            customComponents = list;
            return this;
        }

        Builder typeConfigs(Map<Key, DamageTypeConfig> cfgs) { typeConfigs.putAll(cfgs); return this; }
        Builder customComponents(List<DamageComponent> components) { customComponents = components; return this; }

        public DamageConfig build() {
            return new DamageConfig(this);
        }
    }
}
