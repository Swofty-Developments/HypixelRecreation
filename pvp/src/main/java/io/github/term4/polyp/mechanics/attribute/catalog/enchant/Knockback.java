package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import net.kyori.adventure.key.Key;

/**
 * Knockback - identity key only, not a {@link io.github.term4.polyp.mechanics.attribute.source.Source}:
 * {@code KnockbackCalculator} reads the level off the weapon and folds it in with the sprint bonus (vanilla
 * {@code i = knockbackLevel + (sprinting ? 1 : 0)}). Same in 1.8 and 26.
 */
public final class Knockback {
    public static final Key KEY = Key.key("minecraft:knockback");
    private Knockback() {}
}
