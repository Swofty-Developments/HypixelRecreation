package io.github.term4.polyp.mechanics.damage.types.magic;

import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;

/**
 * Magic damage ({@code minecraft:magic}): instant-harming potions (splash or drunk). Bypasses armor points like
 * vanilla's {@code DamageSource.MAGIC} ({@code setDamageBypassesArmor}, identical 1.8-26); Resistance and Protection
 * still apply. Config-only; callers emit a snapshot with the computed amount (see {@link HealOrHarm}).
 */
public final class MagicDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:magic");
    public static final MagicDamage INSTANCE = new MagicDamage();

    private MagicDamage() {
        super(KEY, "Magic", VanillaTypes.MAGIC,
                DamageTypeConfig.builder(KEY).baseAmount(0.0).bypassArmor(true).build());
    }
}
