package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.mechanics.explosion.TntConfigResolver.TntContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** Immutable primed-TNT config; unset knobs resolve to the vanilla values ({@link PrimedTnt#VANILLA}). */
@GenerateBuilder
public final class TntConfig extends Config<TntContext, TntConfig> {

    public final @Nullable FieldValue<TntContext, Integer> fuseTicks;
    public final @Nullable FieldValue<TntContext, Float> power;
    public final @Nullable FieldValue<TntContext, Boolean> detonateAtFeet;
    public final @Nullable FieldValue<TntContext, PrimedTnt.Wire> wire;
    public final @Nullable FieldValue<TntContext, Boolean> bounce;
    public final @Nullable FieldValue<TntContext, Double> tntVictimScale;
    public final @Nullable FieldValue<TntContext, Boolean> igniteOnPlace;

    private TntConfig(Builder b) {
        super(b.subConfig);
        this.fuseTicks = b.fuseTicks;
        this.power = b.power;
        this.detonateAtFeet = b.detonateAtFeet;
        this.wire = b.wire;
        this.bounce = b.bounce;
        this.tntVictimScale = b.tntVictimScale;
        this.igniteOnPlace = b.igniteOnPlace;
    }

    @Override
    public TntConfig fromBase(TntConfig base) {
        Builder b = new Builder();
        b.subConfig(subConfig != null ? subConfig : base.subConfig);
        b.mergeKnobs(this, base);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public static final class Builder extends TntConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }

        private Function<TntContext, TntConfig> subConfig;

        Builder() {}
        Builder(TntConfig c) {
            super(c);
            subConfig = c.subConfig;
        }

        public Builder subConfig(Function<TntContext, TntConfig> fn) { subConfig = fn; return this; }

        public TntConfig build() { return new TntConfig(this); }
    }
}
