package io.github.term4.polyp.mechanics.blocking;

import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.BlockingContext;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Immutable blocking config, assignable per scope via the {@link io.github.term4.polyp.MechanicsProfile}
 * {@code blocking} member. A {@code Material} blocks iff it has an entry in {@link #materials} - that map is both "which
 * items block" and their per-item config; {@link #defaults} supplies shared knobs every entry inherits. An empty config
 * disables blocking.
 */
public final class BlockingConfig extends Config<BlockingContext, BlockingConfig> {

    /** Shared base every blockable material inherits unless its own entry overrides a knob ({@code null} = none). */
    public final @Nullable BlockingTypeConfig defaults;
    /** Blockable materials -&gt; their per-item config; the keyset is exactly the materials that block. */
    public final Map<Material, BlockingTypeConfig> materials;

    private BlockingConfig(Builder b) {
        super(b.subConfig);
        this.defaults = b.defaults;
        this.materials = Map.copyOf(b.materials);
    }

    public @Nullable BlockingTypeConfig defaults() { return defaults; }
    /** Per-item config for {@code material}, or {@code null} if that material doesn't block. */
    public @Nullable BlockingTypeConfig typeConfig(Material material) { return materials.get(material); }
    /** Whether {@code material} blocks in this scope. */
    public boolean blocks(Material material) { return materials.containsKey(material); }

    /** Merges this config over base (this config's defaults + per-material entries layered over base's). */
    public BlockingConfig fromBase(BlockingConfig base) {
        Map<Material, BlockingTypeConfig> merged = new LinkedHashMap<>(base.materials);
        merged.putAll(materials);
        BlockingTypeConfig mergedDefaults = defaults == null ? base.defaults
                : base.defaults == null ? defaults : defaults.fromBase(base.defaults);
        return new Builder()
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .defaults(mergedDefaults)
                .materials(merged)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }
    public static Builder builder() { return new Builder(); }
    public static Builder builder(@Nullable BlockingConfig base) { return base != null ? new Builder(base) : new Builder(); }

    public static final class Builder {
        private Function<BlockingContext, BlockingConfig> subConfig;
        private @Nullable BlockingTypeConfig defaults;
        private final Map<Material, BlockingTypeConfig> materials = new LinkedHashMap<>();

        Builder() {}
        Builder(BlockingConfig c) { subConfig = c.subConfig; defaults = c.defaults; materials.putAll(c.materials); }

        public Builder subConfig(Function<BlockingContext, BlockingConfig> fn) { subConfig = fn; return this; }
        /** Sets the shared base every blockable material inherits. */
        public Builder defaults(@Nullable BlockingTypeConfig generic) { this.defaults = generic; return this; }

        /** Marks {@code mats} blockable with no per-item override (they inherit {@link #defaults}). */
        public Builder materials(Material... mats) {
            for (Material m : mats) materials.putIfAbsent(m, BlockingTypeConfig.builder().build());
            return this;
        }

        /** Marks {@code material} blockable with a specific per-item config (layered over {@link #defaults}). */
        public Builder material(Material material, BlockingTypeConfig cfg) { materials.put(material, cfg); return this; }

        Builder materials(Map<Material, BlockingTypeConfig> mats) { materials.putAll(mats); return this; }

        public BlockingConfig build() { return new BlockingConfig(this); }
    }
}
