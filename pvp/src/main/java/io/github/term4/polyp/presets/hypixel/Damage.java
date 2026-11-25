package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

/** Hypixel damage: the vanilla 1.8 base with silent overdamage and a 9-tick invul window. */
public final class Damage {

    private Damage() {}

    /** {@code hurtKnockback} inherits Vanilla18's zero-impulse config. */
    public static DamageConfig config() {
        return DamageConfig.builder(Vanilla18.damage())
                .overdamageSilent(true)
                .invulTicks(9)
                .build();
    }
}
