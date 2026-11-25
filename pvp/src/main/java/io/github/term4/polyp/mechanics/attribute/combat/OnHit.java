package io.github.term4.polyp.mechanics.attribute.combat;

/**
 * An {@link io.github.term4.polyp.mechanics.attribute.source.ItemSource} that also fires a side effect when
 * its holder lands a hit (Fire Aspect's ignite); the damage system dispatches it. Knockback enchants feed the KB
 * computation instead, so they aren't {@code OnHit}.
 */
public interface OnHit {

    void onHit(HitContext ctx);
}
