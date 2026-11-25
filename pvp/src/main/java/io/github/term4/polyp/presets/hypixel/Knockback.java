package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

/** Hypixel melee knockback, measured against live servers: the vanilla 1.8 base with one delta. */
public final class Knockback {

    private Knockback() {}

    // measured sprint-hit vertical; vanilla is 0.1
    private static final double EXTRA_VERTICAL = 0.07;

    public static KnockbackConfig melee() {
        return KnockbackConfig.builder(Vanilla18.knockback())
                .extraVertical(EXTRA_VERTICAL)
                .build();
    }
}
