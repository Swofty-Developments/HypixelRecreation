package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Resolves DamageConfig with context into plain values. Mirrors KnockbackConfigResolver. */
public final class DamageConfigResolver {

    private DamageConfigResolver() {}

    public record DamageContext(DamageSnapshot snap, Services services) {
        public static DamageContext of(DamageSnapshot snap, Services services) {
            return new DamageContext(snap, services);
        }

        /** Item involved in the damage (melee weapon), or {@code null}. */
        public @Nullable ItemStack item() { return snap.item(); }

        /** Type-specific payload attached by the producer (e.g. the fall distance), or {@code null}. */
        public @Nullable Object detail() { return snap.detail(); }

        /** The {@link #detail()} payload when it is an instance of {@code type}, else {@code null}. */
        public <T> @Nullable T detail(Class<T> type) {
            Object d = snap.detail();
            return type.isInstance(d) ? type.cast(d) : null;
        }

        /** The effective global {@link DamageConfig}: the snapshot override, else the installed config; {@code null} if neither. */
        public @Nullable DamageConfig damageConfig() {
            DamageConfig cfg = snap.config();
            if (cfg == null && services != null && services.damage() != null) cfg = services.damage().config();
            return cfg;
        }

        /** Effective per-type config: the active {@link DamageConfig}'s override for the type, else the type's default. */
        public DamageTypeConfig typeConfig() {
            DamageConfig cfg = damageConfig();
            DamageTypeConfig tc = cfg != null ? cfg.typeConfig(snap.type().key()) : null;
            // a registered entry replaces the type default outright; unset knobs fall back to the global config
            return (tc != null ? tc : snap.type().defaultConfig()).withOverlay(this);
        }

        /** Base amount before modifiers: snapshot override, else the type's resolved base amount. */
        public float baseAmount() {
            if (snap.amount() != null) return snap.amount();
            Double base = typeConfig().baseAmount(this);
            return base != null ? base.floatValue() : 0f;
        }
    }

    public static ResolvedDamageConfig resolve(DamageConfig config, DamageContext ctx) {
        DamageConfig cfg = config.withOverlay(ctx);

        Integer invulTicks = FieldValue.resolve(cfg.invulTicks, ctx);
        Boolean enableOverdamage = FieldValue.resolve(cfg.enableOverdamage, ctx);
        Boolean silent = FieldValue.resolve(cfg.silent, ctx);
        Boolean overdamageSilent = FieldValue.resolve(cfg.overdamageSilent, ctx);
        Boolean syncHurtVelocity = FieldValue.resolve(cfg.syncHurtVelocity, ctx);
        KnockbackConfig hurtKnockback = FieldValue.resolve(cfg.hurtKnockback, ctx);

        return new ResolvedDamageConfig(ctx.baseAmount(), invulTicks, enableOverdamage, silent, overdamageSilent, syncHurtVelocity, hurtKnockback);
    }

    /** Resolved config with plain values (nullable when unset). Used by DamageCalculator. */
    public record ResolvedDamageConfig(
            float baseAmount,
            @Nullable Integer invulTicks,
            @Nullable Boolean enableOverdamage,
            @Nullable Boolean silent,
            @Nullable Boolean overdamageSilent,
            @Nullable Boolean syncHurtVelocity,
            @Nullable KnockbackConfig hurtKnockback
    ) {}
}
