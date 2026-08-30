package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.mechanics.consumable.ConsumableTypeConfig.ParticleVisibility;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A consumable <em>type</em>: its identity ({@link #key} + the {@link Material}(s) it triggers on) and its
 * {@link #defaultConfig()} baseline. Version-agnostic - per-version behavior/effects come from the active
 * {@link ConsumableConfig}'s per-type override.
 */
public final class Consumable {

    /** 1.6s, food and drink alike. */
    public static final int VANILLA_CONSUME_TICKS = 32;

    private final Key key;
    private final Set<Material> materials;
    private final ConsumableTypeConfig defaultConfig;
    private final @Nullable Material remainder;

    private Consumable(Builder b) {
        this.key = b.key;
        this.materials = Set.copyOf(b.materials);
        this.defaultConfig = b.defaultConfig != null ? b.defaultConfig : ConsumableTypeConfig.builder(b.key).build();
        this.remainder = b.remainder;
    }

    public Key key() { return key; }
    public Set<Material> materials() { return materials; }
    /** Baseline; the resolver layers scope overrides over it. */
    public ConsumableTypeConfig defaultConfig() { return defaultConfig; }
    /** Item left after consuming one, or {@code null} to fall back to the item's {@code use_remainder}. */
    public @Nullable Material remainder() { return remainder; }

    public static Builder builder(Key key, Material... materials) { return new Builder(key, materials); }

    public static final class Builder {
        private final Key key;
        private final List<Material> materials;
        private ConsumableTypeConfig defaultConfig;
        private Integer consumeTicks;
        private ConsumableBehavior behavior;
        private ParticleVisibility particles;
        private Material remainder;

        Builder(Key key, Material... materials) {
            this.key = key;
            this.materials = new ArrayList<>(List.of(materials));
        }

        /** Mutually exclusive with the knob setters below. */
        public Builder defaultConfig(ConsumableTypeConfig cfg) { this.defaultConfig = cfg; return this; }
        public Builder consumeTicks(int v) { this.consumeTicks = v; return this; }
        public Builder behavior(ConsumableBehavior v) { this.behavior = v; return this; }
        public Builder particles(ParticleVisibility v) { this.particles = v; return this; }
        /** Overrides the item's {@code use_remainder}. */
        public Builder remainder(Material v) { this.remainder = v; return this; }

        public Consumable build() {
            if (defaultConfig == null && (consumeTicks != null || behavior != null || particles != null)) {
                ConsumableTypeConfig.Builder b = ConsumableTypeConfig.builder(key);
                if (consumeTicks != null) b.consumeTicks(consumeTicks);
                if (behavior != null) b.behavior(behavior);
                if (particles != null) b.particles(particles);
                defaultConfig = b.build();
            }
            return new Consumable(this);
        }
    }
}
