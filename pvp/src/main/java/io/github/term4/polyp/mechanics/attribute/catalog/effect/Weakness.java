package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Weakness - flat attack-damage debuff in both versions. {@link #LEGACY} is 1.8's {@code -0.5 × level}; {@link #MODERN}
 * is the 1.9+ {@code -4 × level}.
 */
public final class Weakness {

    public static final Key KEY = Key.key("minecraft:weakness");

    private Weakness() {}

    public static final Source LEGACY = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.ATTACK_DAMAGE, AttributeOperation.ADD_VALUE, -0.5 * level));
        }
    };

    public static final Source MODERN = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.ATTACK_DAMAGE, AttributeOperation.ADD_VALUE, -4.0 * level));
        }
    };
}
