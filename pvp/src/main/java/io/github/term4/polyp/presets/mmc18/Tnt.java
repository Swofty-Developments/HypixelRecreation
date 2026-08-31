package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.explosion.TntConfig;
import io.github.term4.polyp.mechanics.explosion.TntConfigResolver;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

/**
 * MineMen TNT: fuse 52, feet detonation, the MINEMEN wire shape (see {@link PrimedTnt}), a ~1.1 push on TNT victims,
 * place-ignition. NOT replicated: their full-block-only collision (MineMen TNT falls through fences).
 */
public final class Tnt {

    private Tnt() {}

    // below-center blast is plain vanilla DOWN at this scale; the "up" on a grounded victim is PrimedTnt's ground
    // bounce, not a rule. Fireball sources keep the profile's KB_SCALE.
    private static final double TNT_VICTIM_SCALE = 1.1;

    public static TntConfig config() {
        return TntConfig.builder().fuseTicks(52).detonateAtFeet(true).wire(PrimedTnt.Wire.MINEMEN)
                .tntVictimScale(TNT_VICTIM_SCALE).igniteOnPlace(true).build();
    }

    public static @Nullable PrimedTnt spawn(ExplosionSystem explosion, Instance instance, Point tntBlock) {
        MechanicsWorld world = MechanicsWorld.of(instance);
        return PrimedTnt.spawn(explosion, world, tntBlock, TntConfigResolver.resolve(config(),
                null, world, TntPrimeEvent.Cause.API, Polyp.getInstance().services()));
    }
}
