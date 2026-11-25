package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import net.kyori.adventure.key.Key;

/**
 * Flame - ignites the struck entity for 5 seconds ({@code EntityArrow.setOnFire(5)}, identical 1.8/26); max level 1, so
 * the duration is fixed. Captured off the launcher like {@link Power}/{@link Punch}, so it is an identity key only, not
 * a {@link io.github.term4.polyp.mechanics.attribute.source.Source}.
 */
public final class Flame {
    public static final Key KEY = Key.key("minecraft:flame");
    public static final int FIRE_TICKS = 5 * 20;
    private Flame() {}
}
