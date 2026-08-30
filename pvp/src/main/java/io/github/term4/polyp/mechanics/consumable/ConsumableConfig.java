package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Immutable consumable config: a generic {@link #defaults} base plus per-consumable {@link ConsumableTypeConfig}
 * overrides keyed by type key. Assigned per scope via the
 * {@link io.github.term4.polyp.MechanicsProfile} {@code consumables} member. Resolution layers per-type
 * override -&gt; {@link #defaults} -&gt; the registered type's {@code defaultConfig()} -&gt; hard fallbacks.
 *
 * <p>The consumable <em>definitions</em> (the {@link Consumable} types + their materials) live in the
 * {@link ConsumableRegistry}; this config only tunes them.
 */
public final class ConsumableConfig extends Config<ConsumableContext, ConsumableConfig> {

    /** Generic base applied to every consumable unless its own entry overrides a knob ({@code null} = none). */
    public final @Nullable ConsumableTypeConfig defaults;
    /** Per-consumable config overrides, keyed by {@link ConsumableTypeConfig#key()}. */
    public final Map<Key, ConsumableTypeConfig> typeConfigs;
    /** Consumable type identities (key + material) this config registers. Read once at install, from the global profile. */
    public final List<Consumable> types;
    /** The {@link ComponentFood} floor: unregistered items with a {@code food} component consume with their registry values (unset = on). */
    public final @Nullable Boolean componentFoods;

    private ConsumableConfig(Builder b) {
        super(b.subConfig);
        this.defaults = b.defaults;
        this.typeConfigs = Map.copyOf(b.typeConfigs);
        this.types = List.copyOf(b.types);
        this.componentFoods = b.componentFoods;
    }

    public @Nullable ConsumableTypeConfig defaults() { return defaults; }

    public @Nullable Boolean componentFoods() { return componentFoods; }

    public @Nullable ConsumableTypeConfig typeConfig(Key key) { return typeConfigs.get(key); }

    public List<Consumable> types() { return types; }

    /** Merges this config over {@code base}; per-type entries and types layer over base's. */
    public ConsumableConfig fromBase(ConsumableConfig base) {
        Map<Key, ConsumableTypeConfig> merged = new LinkedHashMap<>(base.typeConfigs);
        merged.putAll(typeConfigs);
        List<Consumable> mergedTypes = new ArrayList<>(base.types);
        mergedTypes.addAll(types);
        ConsumableTypeConfig mergedDefaults = defaults == null ? base.defaults
                : base.defaults == null ? defaults : defaults.fromBase(base.defaults);
        return new Builder()
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .defaults(mergedDefaults)
                .typeConfigs(merged)
                .types(mergedTypes)
                .componentFoods(componentFoods != null ? componentFoods : base.componentFoods)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable ConsumableConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private Function<ConsumableContext, ConsumableConfig> subConfig;
        private @Nullable ConsumableTypeConfig defaults;
        private final Map<Key, ConsumableTypeConfig> typeConfigs = new LinkedHashMap<>();
        private final List<Consumable> types = new ArrayList<>();
        private @Nullable Boolean componentFoods;

        Builder() {}
        Builder(ConsumableConfig c) {
            subConfig = c.subConfig; defaults = c.defaults; typeConfigs.putAll(c.typeConfigs); types.addAll(c.types);
            componentFoods = c.componentFoods;
        }

        public Builder subConfig(Function<ConsumableContext, ConsumableConfig> fn) { subConfig = fn; return this; }
        public Builder defaults(@Nullable ConsumableTypeConfig generic) { this.defaults = generic; return this; }
        public Builder componentFoods(@Nullable Boolean v) { this.componentFoods = v; return this; }

        /** Each keyed by its {@link ConsumableTypeConfig#key()}. */
        public Builder typeConfigs(ConsumableTypeConfig... cfgs) {
            for (ConsumableTypeConfig c : cfgs) typeConfigs.put(c.key(), c);
            return this;
        }

        Builder typeConfigs(Map<Key, ConsumableTypeConfig> cfgs) { typeConfigs.putAll(cfgs); return this; }

        public Builder types(Consumable... t) { for (Consumable c : t) types.add(c); return this; }

        Builder types(List<Consumable> t) { types.addAll(t); return this; }

        public ConsumableConfig build() { return new ConsumableConfig(this); }
    }
}
