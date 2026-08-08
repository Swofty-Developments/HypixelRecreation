package io.github.term4.polyp.mechanics.attribute;
import io.github.term4.polyp.mechanics.attribute.source.Source;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.FieldValue;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resolves AttributeConfig with context into plain values, and is the query a consumer reads. Mirrors DamageConfigResolver. */
public final class AttributeConfigResolver {

    private AttributeConfigResolver() {}

    /**
     * Resolution + query context for one entity (and in-context item). Active sources are scanned lazily off
     * {@link AttributeSystem}; modifiers pass {@link AttributeConfig#tuningFor tuning} before they fold.
     */
    public record AttributeContext(LivingEntity entity, @Nullable ItemStack item, AttributeConfig config, Services services,
                                   Map<FactKey<?>, Object> facts) {

        public static AttributeContext of(LivingEntity entity, @Nullable ItemStack item, AttributeConfig config, Services services) {
            return new AttributeContext(entity, item, config, services, Map.of());
        }

        /** {@code null} if the calling domain didn't set it. */
        @SuppressWarnings("unchecked")
        public <T> @Nullable T fact(FactKey<T> key) { return (T) facts.get(key); }

        /** A copy with {@code key -> value} added; a no-op if {@code value} is null. */
        public <T> AttributeContext with(FactKey<T> key, @Nullable T value) {
            if (value == null) return this;
            Map<FactKey<?>, Object> next = new HashMap<>(facts);
            next.put(key, value);
            return new AttributeContext(entity, item, config, services, next);
        }

        private List<AttributeSystem.Active> active() {
            AttributeSystem sys = services != null ? services.attributes() : null;
            if (sys == null) return List.of();
            if (Boolean.FALSE.equals(FieldValue.resolve(config.enabled, this))) return List.of();
            return sys.activeSources(entity, item);
        }

        private List<Source.Mod> tuned(AttributeSystem.Active a) {
            return config.tuningFor(a.source().key()).apply(this, a.source().modifiers(a.level(), this));
        }

        /** {@code base} folded with the active modifiers for {@code attr}; custom attributes pass base 0. */
        public double value(Attribute attr, double base) {
            double add = 0, multBase = 0, multTotal = 1;
            for (AttributeSystem.Active a : active()) {
                for (Source.Mod m : tuned(a)) {
                    if (!m.attribute().equals(attr)) continue;
                    switch (m.operation()) {
                        case ADD_VALUE -> add += m.amount();
                        case ADD_MULTIPLIED_BASE -> multBase += m.amount();
                        case ADD_MULTIPLIED_TOTAL -> multTotal *= (1 + m.amount());
                    }
                }
            }
            return (base + add) * (1 + multBase) * multTotal;
        }

        /** Amplifier + 1, or 0 if absent. */
        public int effectLevel(Key effectKey) {
            for (TimedPotion tp : entity.getActiveEffects()) {
                if (tp.potion().effect().key().equals(effectKey)) return tp.potion().amplifier() + 1;
            }
            return 0;
        }
    }

    public static ResolvedAttributeConfig resolve(AttributeConfig config, AttributeContext ctx) {
        AttributeConfig cfg = config.withOverlay(ctx);
        return new ResolvedAttributeConfig(FieldValue.resolve(cfg.enabled, ctx));
    }

    public record ResolvedAttributeConfig(@Nullable Boolean enabled) {}
}
