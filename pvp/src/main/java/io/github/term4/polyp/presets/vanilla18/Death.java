package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.death.DeathConfig;

/** Vanilla 1.8 death/respawn cleanup defaults. */
public final class Death {

    private Death() {}

    public static DeathConfig config() {
        return DeathConfig.builder()
                .clearEffects(true)
                .resetMechanicsState(true)
                .hideCorpse(true)
                .deathAnimationTicks(20)
                .build();
    }
}
