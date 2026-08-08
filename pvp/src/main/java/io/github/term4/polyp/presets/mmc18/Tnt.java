package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import io.github.term4.polyp.entity.PrimedTnt;

/**
 * MineMen TNT: fuse 52, feet detonation, the MINEMEN wire shape (see {@link PrimedTnt}), and a ~1.1 push on TNT victims.
 * NOT replicated: their full-block-only collision (MineMen TNT falls through fences).
 */
public final class Tnt {

    private Tnt() {}

    // below-center blast is plain vanilla DOWN at this scale; the "up" on a grounded victim is PrimedTnt's ground
    // bounce, not a rule. Fireball sources keep the profile's KB_SCALE.
    private static final double TNT_VICTIM_SCALE = 1.1;

    public static final PrimedTnt.Config CONFIG =
            new PrimedTnt.Config(52, 4.0f, true, PrimedTnt.Wire.MINEMEN, true, TNT_VICTIM_SCALE);

    public static PrimedTnt spawn(ExplosionSystem explosion, Instance instance, Point tntBlock) {
        return PrimedTnt.spawn(explosion, instance, tntBlock, CONFIG);
    }
}
