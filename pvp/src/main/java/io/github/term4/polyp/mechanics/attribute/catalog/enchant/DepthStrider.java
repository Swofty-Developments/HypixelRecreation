package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.source.ArmorSource;
import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Depth Strider - {@code +1/3 × level} on the wearer's {@code WATER_MOVEMENT_EFFICIENCY} (full at level 3). Consumed by
 * Minestom physics / the client, so there is no separate calc.
 */
public final class DepthStrider {

    public static final Key KEY = Key.key("minecraft:depth_strider");

    private DepthStrider() {}

    public static final Source INSTANCE = new ArmorSource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.WATER_MOVEMENT_EFFICIENCY, AttributeOperation.ADD_VALUE, level / 3.0));
        }
    };
}
