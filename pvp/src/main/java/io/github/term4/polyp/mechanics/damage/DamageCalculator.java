package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.ResolvedDamageConfig;

/**
 * Computes the base damage amount for a snapshot (type-specific modifiers are applied by the producer first). Mirrors
 * KnockbackCalculator.
 */
public final class DamageCalculator {

    private final Services services;
    private final DamageConfig defaults;

    public DamageCalculator(Services services, DamageConfig defaults) {
        this.services = services;
        this.defaults = defaults;
    }

    /** Merges the snapshot's config (if any) over the defaults, then resolves it. */
    public ResolvedDamageConfig resolveConfig(DamageSnapshot snap) {
        DamageConfig base = snap.config() != null ? snap.config() : defaults;
        DamageConfig merged = base.fromBase(defaults);
        return DamageConfigResolver.resolve(merged, DamageContext.of(snap, services));
    }

    public DamageResult compute(DamageSnapshot snap) {
        ResolvedDamageConfig cfg = resolveConfig(snap);
        return new DamageResult(cfg.baseAmount());
    }

    public record DamageResult(float amount) {}
}
