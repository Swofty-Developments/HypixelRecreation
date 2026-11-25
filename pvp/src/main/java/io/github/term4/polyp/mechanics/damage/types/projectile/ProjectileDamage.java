package io.github.term4.polyp.mechanics.damage.types.projectile;

import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;

import java.util.Set;

/**
 * Thrown-projectile damage ({@code minecraft:thrown}): snowball / egg hits. Vanilla deals {@code 0} but the hit still
 * goes through {@code damageEntity} - it plays the hurt animation and opens the invul window that gates further hits.
 * So {@code baseAmount = 0} with {@code triggersInvul} on; the directional knockback rides the {@code KnockbackSystem}.
 */
public final class ProjectileDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:thrown");
    public static final ProjectileDamage INSTANCE = new ProjectileDamage();

    private ProjectileDamage() {
        super(KEY, "Thrown", VanillaTypes.GENERIC,
                DamageTypeConfig.builder(KEY).baseAmount(0.0).build());
    }

    @Override public Set<ProtectionCategory> protectionCategories() { return Set.of(ProtectionCategory.PROJECTILE); }
}
