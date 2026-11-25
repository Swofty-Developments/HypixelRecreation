package io.github.term4.polyp.mechanics.attribute.source;

import net.kyori.adventure.key.Key;

/** An enchant matched against an in-context item, read on demand during a calculation and never pushed. */
public abstract class ItemSource extends Source {
    protected ItemSource(Key key) { super(key); }
}
