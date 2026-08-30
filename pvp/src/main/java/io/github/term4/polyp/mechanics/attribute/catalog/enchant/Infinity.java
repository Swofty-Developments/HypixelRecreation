package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import net.kyori.adventure.key.Key;

/**
 * Infinity - the bow consumes no arrows (but still needs one in the quiver) and its arrows can't be picked up in
 * survival. Identity key only: {@code Bow} reads the level off the held bow. Same in 1.8 and 26.
 */
public final class Infinity {
    public static final Key KEY = Key.key("minecraft:infinity");
    private Infinity() {}
}
