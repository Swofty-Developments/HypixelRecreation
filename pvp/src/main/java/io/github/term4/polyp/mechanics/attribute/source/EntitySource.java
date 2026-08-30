package io.github.term4.polyp.mechanics.attribute.source;

import net.kyori.adventure.key.Key;

/**
 * A potion effect. Matched against the entity's active effects (level = amplifier + 1); modifiers are pushed/removed on
 * the entity's Minestom instances and {@link Behavior} fires on apply/remove/tick.
 */
public abstract class EntitySource extends Source {
    protected EntitySource(Key key) { super(key); }
}
