package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Strength. {@link #LEGACY} is 1.8's multiplicative {@code ×(1 + 1.3 × level)} ({@code MobEffectAttackDamage});
 * {@link #MODERN} is the 1.9+ flat {@code +3 × level}.
 */
public final class Strength {

    public static final Key KEY = Key.key("minecraft:strength");

    private Strength() {}

    public static final Source LEGACY = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.ATTACK_DAMAGE, AttributeOperation.ADD_MULTIPLIED_TOTAL, 1.3 * level));
        }
    };

    public static final Source MODERN = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.ATTACK_DAMAGE, AttributeOperation.ADD_VALUE, 3.0 * level));
        }
    };
}
