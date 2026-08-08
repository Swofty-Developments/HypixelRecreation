package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.AttributeContext;
import io.github.term4.polyp.mechanics.attribute.source.ItemSource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.combat.CombatFacts;
import io.github.term4.polyp.mechanics.attribute.combat.MonsterTypes;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Smite - {@link Sharpness} but victim-conditional: {@code 2.5 × level} (identical 1.8/26), gated on the combat
 * {@link CombatFacts#TARGET target} being undead.
 */
public final class Smite {

    public static final Key KEY = Key.key("minecraft:smite");
    private static final double PER_LEVEL = 2.5;

    private Smite() {}

    public static final Source INSTANCE = new ItemSource(KEY) {
        @Override public List<Mod> modifiers(int level, AttributeContext ctx) {
            Entity target = ctx.fact(CombatFacts.TARGET);
            if (level <= 0 || !MonsterTypes.isUndead(target)) return List.of();
            return List.of(new Mod(Attribute.MELEE_FLAT_ADD, AttributeOperation.ADD_VALUE, PER_LEVEL * level));
        }
    };
}
