package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;

/**
 * Blindness - purely client-rendered off the stored effect, so no modifier or server behavior. Registered anyway to stay
 * a discoverable, scope-disableable catalog entry.
 */
public final class Blindness {

    public static final Key KEY = Key.key("minecraft:blindness");

    private Blindness() {}

    public static final Source INSTANCE = new EntitySource(KEY) {};
}
