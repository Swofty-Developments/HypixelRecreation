package io.github.term4.polyp.mechanics.attribute.combat;

import io.github.term4.polyp.mechanics.attribute.FactKey;
import net.minestom.server.entity.Entity;

/**
 * Combat-domain facts the attack path drops onto an {@code AttributeContext}, for victim-conditional enchants (Smite/Bane).
 * A neutral {@link Entity} value, so a catalog enchant reads the target without importing the damage package.
 */
public final class CombatFacts {
    private CombatFacts() {}

    /** Absent outside a combat read. */
    public static final FactKey<Entity> TARGET = new FactKey<>("polyp:combat/target");
}
