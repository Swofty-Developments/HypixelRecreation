package io.github.term4.polyp.mechanics.blocking;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.BlockingContext;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

/**
 * Per-item blocking knobs, held in a {@link BlockingConfig} keyed by {@code Material}: every value is a {@link FieldValue}
 * resolved against a {@link BlockingContext} (constant or per-hit), unset fields falling back per-material entry -&gt;
 * {@link BlockingConfig#defaults()} -&gt; hard fallbacks.
 */
@GenerateBuilder
public final class BlockingTypeConfig extends Config<BlockingContext, BlockingTypeConfig> {

    /** Whether this item blocks (default {@code true}); {@code false} disables blocking for it in the scope. */
    public final @Nullable FieldValue<BlockingContext, Boolean> enabled;
    /** How blocking works for this item. Default {@link BlockingBehavior#SWORD}. */
    public final @Nullable FieldValue<BlockingContext, BlockingBehavior> behavior;
    /** Reduction curve base: damage removed {@code = clamp(base + factor*damage)}. 1.8 sword {@code -0.5}. Default {@code 0}. */
    public final @Nullable FieldValue<BlockingContext, Double> reductionBase;
    /** Reduction curve factor. 1.8 sword {@code 0.5}; full block {@code 1.0}. Default {@code 1.0}. */
    public final @Nullable FieldValue<BlockingContext, Double> reductionFactor;
    /** Shield: ticks of holding before the block activates. Default {@code 0} (instant, sword). */
    public final @Nullable FieldValue<BlockingContext, Integer> blockDelayTicks;
    /** Shield: max angle (deg) off the defender's facing that still blocks; {@code null} = omnidirectional (sword). */
    public final @Nullable FieldValue<BlockingContext, Double> blockingAngle;
    /** Shield: damage-type keys this item is bypassed by (never blocks them). Default empty. */
    public final @Nullable FieldValue<BlockingContext, Set<Key>> bypassedTypes;

    BlockingTypeConfig(Builder b) {
        super(b.subConfig);
        this.enabled = b.enabled;
        this.behavior = b.behavior;
        this.reductionBase = b.reductionBase;
        this.reductionFactor = b.reductionFactor;
        this.blockDelayTicks = b.blockDelayTicks;
        this.blockingAngle = b.blockingAngle;
        this.bypassedTypes = b.bypassedTypes;
    }

    /** Merges this config over {@code base}: this config's set fields win, unset fields fall back per resolution. */
    public BlockingTypeConfig fromBase(BlockingTypeConfig base) {
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        b.subConfig = subConfig != null ? subConfig : base.subConfig;
        return b.build();
    }

    public static Builder builder() { return new Builder(); }
    public static Builder builder(BlockingTypeConfig base) { return new Builder(base); }
    public Builder toBuilder() { return new Builder(this); }

    public static final class Builder extends BlockingTypeConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<BlockingContext, BlockingTypeConfig> subConfig;

        Builder() {}

        Builder(BlockingTypeConfig c) {
            super(c);
            subConfig = c.subConfig;
        }

        public Builder subConfig(Function<BlockingContext, BlockingTypeConfig> fn) { subConfig = fn; return this; }

        public BlockingTypeConfig build() { return new BlockingTypeConfig(this); }
    }
}
