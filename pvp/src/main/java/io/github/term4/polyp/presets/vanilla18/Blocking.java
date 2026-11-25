package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.blocking.BlockingBehavior;
import io.github.term4.polyp.mechanics.blocking.BlockingConfig;
import io.github.term4.polyp.mechanics.blocking.BlockingTypeConfig;
import io.github.term4.polyp.mechanics.blocking.catalog.VanillaBlocking;

/**
 * Vanilla 1.8 sword blocking: a blocked hit becomes {@code (1 + f) * 0.5} pre-armor ({@code EntityHuman.damageEntity});
 * omnidirectional, no server-side movement slowdown (client-predicted), and only non-armor-bypassing damage.
 */
public final class Blocking {

    private Blocking() {}

    public static BlockingConfig config() {
        return BlockingConfig.builder()
                .defaults(BlockingTypeConfig.builder()
                        .behavior(BlockingBehavior.SWORD)
                        .reductionBase(-0.5).reductionFactor(0.5)
                        .build())
                .materials(VanillaBlocking.SWORDS)
                .build();
    }
}
