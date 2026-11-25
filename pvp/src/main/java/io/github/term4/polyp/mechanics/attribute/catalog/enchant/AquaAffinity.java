package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.source.ArmorSource;
import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Aqua Affinity - 26's {@code +4 × level} on the wearer's {@code SUBMERGED_MINING_SPEED}. Inert until a mining
 * calculator consumes the attribute; 1.8 instead drops the underwater 5x penalty there.
 */
public final class AquaAffinity {

    public static final Key KEY = Key.key("minecraft:aqua_affinity");

    private AquaAffinity() {}

    public static final Source INSTANCE = new ArmorSource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.SUBMERGED_MINING_SPEED, AttributeOperation.ADD_MULTIPLIED_TOTAL, 4.0 * level));
        }
    };
}
