package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import net.kyori.adventure.key.Key;

/**
 * Punch - identity key only, not a {@link io.github.term4.polyp.mechanics.attribute.source.Source}: the
 * projectile captures the level off the launching item and feeds it as the extra-knockback level, a separate
 * motion-direction knockback (vanilla {@code 0.6 × level} horizontal). Same in 1.8 and 26.
 */
public final class Punch {
    public static final Key KEY = Key.key("minecraft:punch");
    private Punch() {}
}
