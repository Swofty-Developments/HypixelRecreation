package io.github.term4.polyp.mechanics.hunger;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config for the hunger subsystem, assigned per scope via the {@code hunger} profile member. The PRESETS declare
 * every cost: each source charges through its {@link ExhaustionCost} entry (no entry = {@link ExhaustionCost#dynamic()}
 * for custom keys, inert for lib keys), times the global {@code exhaustionScale}.
 */
public final class HungerConfig {

    private final @Nullable Boolean enabled;
    private final @Nullable Boolean naturalRegen;
    private final @Nullable Integer regenFoodThreshold;
    private final @Nullable Integer regenInterval;
    private final @Nullable Boolean saturationRegen;
    private final @Nullable Float exhaustionScale;
    /** Per-source cost rules, keyed by the {@link HungerSystem#exhaust} source. */
    public final Map<Key, ExhaustionCost> exhaustionCosts;

    private HungerConfig(Builder b) {
        this.enabled = b.enabled;
        this.naturalRegen = b.naturalRegen;
        this.regenFoodThreshold = b.regenFoodThreshold;
        this.regenInterval = b.regenInterval;
        this.saturationRegen = b.saturationRegen;
        this.exhaustionScale = b.exhaustionScale;
        this.exhaustionCosts = Map.copyOf(b.exhaustionCosts);
    }

    /** Unset = active. */
    public @Nullable Boolean enabled() { return enabled; }

    /** The {@code naturalRegeneration} gamerule analog (unset = on). */
    public @Nullable Boolean naturalRegen() { return naturalRegen; }

    /** Food level required to regenerate (vanilla 18, both versions). */
    public @Nullable Integer regenFoodThreshold() { return regenFoodThreshold; }

    /** Ticks between regen heals (vanilla 80, both versions). */
    public @Nullable Integer regenInterval() { return regenInterval; }

    /** Modern saturation fast regen: at food 20 with saturation left, heal {@code min(sat,6)/6} every 10 ticks (1.9+; 1.8 has none). */
    public @Nullable Boolean saturationRegen() { return saturationRegen; }

    /** Global multiplier on every exhaustion cost (unset = 1; 0 = hunger never depletes, regen still heals). */
    public @Nullable Float exhaustionScale() { return exhaustionScale; }

    /** The cost rule for {@code source}, or null = {@link ExhaustionCost#dynamic()}. */
    public @Nullable ExhaustionCost exhaustionCost(Key source) { return exhaustionCosts.get(source); }

    /** Merges this config over {@code base}; per-source costs overlay entry-wise. */
    public HungerConfig fromBase(HungerConfig base) {
        Map<Key, ExhaustionCost> mergedCosts = new LinkedHashMap<>(base.exhaustionCosts);
        mergedCosts.putAll(exhaustionCosts);
        return new Builder()
                .enabled(enabled != null ? enabled : base.enabled)
                .naturalRegen(naturalRegen != null ? naturalRegen : base.naturalRegen)
                .regenFoodThreshold(regenFoodThreshold != null ? regenFoodThreshold : base.regenFoodThreshold)
                .regenInterval(regenInterval != null ? regenInterval : base.regenInterval)
                .saturationRegen(saturationRegen != null ? saturationRegen : base.saturationRegen)
                .exhaustionScale(exhaustionScale != null ? exhaustionScale : base.exhaustionScale)
                .exhaustionCosts(mergedCosts)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable HungerConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private @Nullable Boolean enabled;
        private @Nullable Boolean naturalRegen;
        private @Nullable Integer regenFoodThreshold;
        private @Nullable Integer regenInterval;
        private @Nullable Boolean saturationRegen;
        private @Nullable Float exhaustionScale;
        private final Map<Key, ExhaustionCost> exhaustionCosts = new LinkedHashMap<>();

        Builder() {}
        Builder(HungerConfig c) {
            enabled = c.enabled;
            naturalRegen = c.naturalRegen;
            regenFoodThreshold = c.regenFoodThreshold;
            regenInterval = c.regenInterval;
            saturationRegen = c.saturationRegen;
            exhaustionScale = c.exhaustionScale;
            exhaustionCosts.putAll(c.exhaustionCosts);
        }

        public Builder enabled(@Nullable Boolean v) { this.enabled = v; return this; }
        public Builder naturalRegen(@Nullable Boolean v) { this.naturalRegen = v; return this; }
        public Builder regenFoodThreshold(@Nullable Integer v) { this.regenFoodThreshold = v; return this; }
        public Builder regenInterval(@Nullable Integer v) { this.regenInterval = v; return this; }
        public Builder saturationRegen(@Nullable Boolean v) { this.saturationRegen = v; return this; }
        public Builder exhaustionScale(@Nullable Float v) { this.exhaustionScale = v; return this; }
        public Builder exhaustionCost(Key source, ExhaustionCost cost) { exhaustionCosts.put(source, cost); return this; }
        Builder exhaustionCosts(Map<Key, ExhaustionCost> costs) { exhaustionCosts.putAll(costs); return this; }

        public HungerConfig build() { return new HungerConfig(this); }
    }
}
