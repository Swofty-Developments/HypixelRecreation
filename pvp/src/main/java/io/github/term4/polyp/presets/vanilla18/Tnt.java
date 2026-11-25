package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.explosion.TntConfig;

/** Vanilla 1.8 TNT - the resolver defaults: 80-tick fuse, +height/16 detonation, ground bounce, no place-ignition. */
public final class Tnt {

    private Tnt() {}

    public static TntConfig config() {
        return TntConfig.builder().build();
    }
}
