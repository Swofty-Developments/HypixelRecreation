package io.github.term4.polyp.mechanics.attribute.source;

import net.kyori.adventure.key.Key;

/**
 * An enchant active while its armor piece is worn. Modifiers pushed/cleared on the equipment lifecycle; {@link Behavior}
 * fires on equip/unequip and ticks while worn. Level = highest across the four pieces.
 */
public abstract class ArmorSource extends Source {
    protected ArmorSource(Key key) { super(key); }
}
