package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import net.kyori.adventure.key.Key;

/**
 * Power - identity key only, not a {@link io.github.term4.polyp.mechanics.attribute.source.Source}: the
 * projectile captures the level off the launcher and adds {@code 0.5 × level + 0.5} to the hit damage. Captured off any
 * launcher, so a preset can put Power on non-arrow projectiles.
 */
public final class Power {
    public static final Key KEY = Key.key("minecraft:power");
    private Power() {}
}
