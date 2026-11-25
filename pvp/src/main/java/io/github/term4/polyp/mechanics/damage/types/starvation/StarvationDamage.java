package io.github.term4.polyp.mechanics.damage.types.starvation;

import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;

/**
 * Starvation ({@code minecraft:starve}). Produced by the hunger food tick (food 0, same 80-tick timer as regen), not a
 * self-driven ticker; tunables come from the type's {@link DamageTypeConfig}.
 */
public final class StarvationDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:starve");
    public static final StarvationDamage INSTANCE = new StarvationDamage();

    private StarvationDamage() {
        super(KEY, "Starvation", VanillaTypes.STARVE, DamageTypeConfig.builder(KEY).build());
    }
}
