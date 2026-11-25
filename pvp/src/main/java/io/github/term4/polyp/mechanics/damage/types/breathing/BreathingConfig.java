package io.github.term4.polyp.mechanics.damage.types.breathing;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Config for drowning ({@link DrowningDamage}): the air pool + the version-specific refill model, on top of the common
 * {@link DamageTypeConfig} tunables. The drowning/respiration/water-breathing logic is identical across 1.8 and 26; the
 * <em>only</em> difference is {@link AirRefill}. Suffocation uses a plain {@code DamageTypeConfig} (just {@code baseAmount}).
 */
@GenerateBuilder
public final class BreathingConfig extends DamageTypeConfig {

    /** How air recovers when the entity can breathe: {@link #LEGACY} (1.8: snap to max, out of water only) or {@link #MODERN} (26: {@code +4}/tick, in water too while breathing). */
    public enum AirRefill { LEGACY, MODERN }

    public final @Nullable FieldValue<DamageContext, Integer> maxAir;
    public final @Nullable FieldValue<DamageContext, AirRefill> airRefill;

    private BreathingConfig(Builder b) {
        super(b.common);
        this.maxAir = b.maxAir;
        this.airRefill = b.airRefill;
    }

    /** Air pool size (vanilla 300 = 15s submerged before drowning starts). */
    public @Nullable Integer maxAir(DamageContext ctx) { return resolve(maxAir, ctx); }
    /** The air-refill model ({@link AirRefill}); 1.8 = LEGACY, 26 = MODERN. */
    public @Nullable AirRefill airRefill(DamageContext ctx) { return resolve(airRefill, ctx); }

    @Override
    public DamageTypeConfig fromBase(DamageTypeConfig base) {
        DamageTypeConfig mergedCommon = super.fromBase(base);
        Builder b = new Builder();
        b.common.copyFrom(mergedCommon);
        if (base instanceof BreathingConfig f) b.mergeKnobs(this, f);
        else b.copyKnobs(this);
        return b.build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends BreathingConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private final DamageTypeConfig.Builder common = new DamageTypeConfig.Builder();

        public Builder key(Key key) { common.key(key); return this; }
        public Builder enabled(Boolean v) { common.enabled(v); return this; }
        public Builder enabled(Function<DamageContext, Boolean> fn) { common.enabled(fn); return this; }
        public Builder baseAmount(Double v) { common.baseAmount(v); return this; }
        public Builder baseAmount(Function<DamageContext, Double> fn) { common.baseAmount(fn); return this; }
        public Builder invulTicks(Integer v) { common.invulTicks(v); return this; }
        public Builder bypassArmor(Boolean v) { common.bypassArmor(v); return this; }

        public BreathingConfig build() { return new BreathingConfig(this); }
    }
}
