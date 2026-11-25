package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Haste. {@link #MODERN} is 26's {@code +0.1 × level} attack speed. 1.8 Haste is a dig-speed mechanic with no attribute
 * (needs the mining system), so there is no LEGACY variant yet.
 */
public final class Haste {

    public static final Key KEY = Key.key("minecraft:haste");

    private Haste() {}

    public static final Source MODERN = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.ATTACK_SPEED, AttributeOperation.ADD_MULTIPLIED_TOTAL, 0.1 * level));
        }
    };
}
