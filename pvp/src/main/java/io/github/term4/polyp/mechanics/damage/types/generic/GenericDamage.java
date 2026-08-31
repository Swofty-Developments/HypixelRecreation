package io.github.term4.polyp.mechanics.damage.types.generic;

import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;

/**
 * Generic catch-all damage ({@code minecraft:generic}). Config-only; driven externally by callers
 * that emit a snapshot with this type.
 */
public final class GenericDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:generic");
    public static final GenericDamage INSTANCE = new GenericDamage();

    private GenericDamage() {
        super(KEY, "Generic", VanillaTypes.GENERIC,
                DamageTypeConfig.builder(KEY).baseAmount(1.0).build());
    }
}
