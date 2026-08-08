package io.github.term4.polyp.mechanics.attribute;
import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.mechanics.attribute.source.Source;

import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.AttributeContext;
import io.github.term4.polyp.mechanics.attribute.defense.ArmorConfig;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Immutable attribute-system config: the {@link Source} catalog, the {@code enabled} switch, and per-source <em>tunings</em>.
 * Resolvable per scope (player → instance → global). Version = which source variants you register
 * ({@code Strength.LEGACY} vs {@code MODERN}); {@link Builder#disable}/{@link Builder#scale}/{@link Builder#tune} adjust per scope without a new source.
 */
@GenerateBuilder
public final class AttributeConfig extends Config<AttributeContext, AttributeConfig> {

    /** A per-source transform on its resolved modifiers, applied in the source's scope. */
    @FunctionalInterface
    public interface Tuning {
        List<Source.Mod> apply(AttributeContext ctx, List<Source.Mod> mods);
    }

    public static final Tuning IDENTITY = (ctx, mods) -> mods;

    public final FieldValue<AttributeContext, Boolean> enabled;
    private final List<Source> sources;
    private final Map<Key, Tuning> tunings;

    /** {@code null} = no armor reduction. */
    @Nullable public final ArmorConfig armor;

    /** {@code null} = no enchant protection. */
    @Nullable public final ProtectionConfig protection;

    /** {@code null} = the vanilla 20% ({@code 25 − 5·level} integer curve). */
    @Nullable public final Double resistancePerLevel;

    /** {@code null} = vanilla order (armor, resistance, protection). */
    @Nullable public final List<MitigationPipeline.Stage> mitigationStages;

    /**
     * {@code true}: held modifiers ride the per-tick reconcile (vanilla {@code detectEquipmentUpdates} timing), so a hotbar
     * swap lags a tick - the exploitable window. {@code false} (default): refreshed on the slot change (Paper's
     * {@code updateEquipmentOnPlayerActions}). Armor always rides the tick, avoiding the use-item prediction race.
     */
    @Nullable public final Boolean attributeSwapping;

    private AttributeConfig(Builder b) {
        super(b.subConfig);
        this.enabled = b.enabled;
        this.sources = b.sources != null ? List.copyOf(b.sources) : List.of();
        this.tunings = b.tunings != null ? Map.copyOf(b.tunings) : Map.of();
        this.armor = b.armor;
        this.protection = b.protection;
        this.resistancePerLevel = b.resistancePerLevel;
        this.mitigationStages = b.mitigationStages;
        this.attributeSwapping = b.attributeSwapping;
    }

    public boolean attributeSwapping() { return attributeSwapping != null && attributeSwapping; }

    /** Sources to register at install. */
    public List<Source> sources() { return sources; }

    /** The tuning for {@code key}, or {@link #IDENTITY} when unset. */
    public Tuning tuningFor(Key key) { return tunings.getOrDefault(key, IDENTITY); }

    /** Merges this config over base: this wins per key; tunings union with this overriding. */
    public AttributeConfig fromBase(AttributeConfig base) {
        Map<Key, Tuning> merged = new HashMap<>(base.tunings);
        merged.putAll(tunings);
        Builder b = new Builder();
        b.mergeKnobs(this, base);
        return b
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .sources(!sources.isEmpty() ? sources : base.sources)
                .tunings(merged)
                .armor(armor != null ? (base.armor != null ? armor.fromBase(base.armor) : armor) : base.armor)
                .protection(protection != null ? (base.protection != null ? protection.fromBase(base.protection) : protection) : base.protection)
                .resistancePerLevel(resistancePerLevel != null ? resistancePerLevel : base.resistancePerLevel)
                .mitigationStages(mitigationStages != null ? mitigationStages : base.mitigationStages)
                .attributeSwapping(attributeSwapping != null ? attributeSwapping : base.attributeSwapping)
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AttributeConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }
        private Function<AttributeContext, AttributeConfig> subConfig;
        private List<Source> sources;
        private Map<Key, Tuning> tunings;
        private ArmorConfig armor;
        private ProtectionConfig protection;
        private Double resistancePerLevel;
        private List<MitigationPipeline.Stage> mitigationStages;
        private Boolean attributeSwapping;

        Builder() {}

        Builder(AttributeConfig c) {
            super(c);
            subConfig = c.subConfig;
            sources = c.sources.isEmpty() ? null : new ArrayList<>(c.sources);
            tunings = c.tunings.isEmpty() ? null : new HashMap<>(c.tunings);
            armor = c.armor;
            protection = c.protection;
            resistancePerLevel = c.resistancePerLevel;
            mitigationStages = c.mitigationStages;
            attributeSwapping = c.attributeSwapping;
        }

        public Builder subConfig(Function<AttributeContext, AttributeConfig> fn) { subConfig = fn; return this; }

        /** Adds sources to the catalog. */
        public Builder sources(Source... add) {
            List<Source> list = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
            for (Source s : add) list.add(s);
            sources = list;
            return this;
        }

        /** Turns a source off in this scope. */
        public Builder disable(Key sourceKey) { return tune(sourceKey, (ctx, mods) -> List.of()); }

        /** Scales a source's modifier amounts in this scope. */
        public Builder scale(Key sourceKey, double factor) {
            return tune(sourceKey, (ctx, mods) -> {
                List<Source.Mod> out = new ArrayList<>(mods.size());
                for (Source.Mod m : mods) out.add(new Source.Mod(m.attribute(), m.operation(), m.amount() * factor));
                return out;
            });
        }

        /** Replaces a source's modifier output in this scope. */
        public Builder tune(Key sourceKey, Tuning tuning) {
            if (tunings == null) tunings = new HashMap<>();
            tunings.put(sourceKey, tuning);
            return this;
        }

        public Builder armor(ArmorConfig v) { armor = v; return this; }

        public Builder protection(ProtectionConfig v) { protection = v; return this; }

        /** {@code null} = the vanilla 20% integer curve. */
        public Builder resistancePerLevel(Double v) { resistancePerLevel = v; return this; }
        /** Replaces the stage list, order included; seed from {@link MitigationPipeline#vanilla()}. */
        public Builder mitigationStages(List<MitigationPipeline.Stage> v) { mitigationStages = v; return this; }

        /** See {@link AttributeConfig#attributeSwapping}. */
        public Builder attributeSwapping(Boolean v) { attributeSwapping = v; return this; }

        Builder sources(List<Source> v) { sources = v; return this; }
        Builder tunings(Map<Key, Tuning> v) { tunings = v; return this; }

        public AttributeConfig build() { return new AttributeConfig(this); }
    }
}
