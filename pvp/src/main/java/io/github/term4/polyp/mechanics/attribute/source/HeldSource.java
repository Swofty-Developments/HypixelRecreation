package io.github.term4.polyp.mechanics.attribute.source;

import net.kyori.adventure.key.Key;

/**
 * An enchant pushed onto the holder while its item is in the main hand, reconciled on held-item/hotbar changes. Unlike
 * {@link ItemSource} the attribute must reach the holder because mining is client-computed; 1.8 reads the enchant off the
 * item itself, so this only matters for modern clients.
 */
public abstract class HeldSource extends Source {
    protected HeldSource(Key key) { super(key); }
}
