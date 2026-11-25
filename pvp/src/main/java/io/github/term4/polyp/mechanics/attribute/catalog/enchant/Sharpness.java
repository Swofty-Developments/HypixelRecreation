package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.ItemSource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Sharpness - a flat melee bonus added after crit, via {@link Attribute#MELEE_FLAT_ADD}. {@link #LEGACY} is 1.8's
 * {@code 1.25 × level} ({@code EnchantmentWeaponDamage}, "all"); {@link #MODERN} is {@code 0.5 × level + 0.5}.
 */
public final class Sharpness {

    public static final Key KEY = Key.key("minecraft:sharpness");

    private Sharpness() {}

    public static final Source LEGACY = new ItemSource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.MELEE_FLAT_ADD, AttributeOperation.ADD_VALUE, 1.25 * level));
        }
    };

    public static final Source MODERN = new ItemSource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.MELEE_FLAT_ADD, AttributeOperation.ADD_VALUE, 0.5 * level + 0.5));
        }
    };
}
