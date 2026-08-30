package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import io.github.term4.polyp.entity.PrimedTnt;

/** Hypixel TNT: fuse 50 (vanilla 80), FEET detonation (capture: expl.y − feet = 0 across 11 TNTs, not vanilla +height/16 nor Spigot +length/2), HYPIXEL wire shape, no ground bounce. */
public final class Tnt {

    private Tnt() {}

    // tntVictimScale null: Hypixel's explosion KB is already vanilla 1.0
    public static final PrimedTnt.Config CONFIG = new PrimedTnt.Config(50, 4.0f, true, PrimedTnt.Wire.HYPIXEL, false, null);

    public static PrimedTnt spawn(ExplosionSystem explosion, Instance instance, Point tntBlock) {
        return PrimedTnt.spawn(explosion, instance, tntBlock, CONFIG);
    }
}
