package io.github.term4.polyp.presets.hypixel;

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

/** Hypixel TNT: fuse 50 (vanilla 80), FEET detonation (capture: expl.y − feet = 0 across 11 TNTs, not vanilla +height/16 nor Spigot +length/2), HYPIXEL wire shape, no ground bounce, BedWars place-ignition. */
public final class Tnt {

    private Tnt() {}

    // tntVictimScale unset: Hypixel's explosion KB is already vanilla 1.0
    public static TntConfig config() {
        return TntConfig.builder().fuseTicks(50).detonateAtFeet(true).bounce(false).igniteOnPlace(true).build();
    }

    public static @Nullable PrimedTnt spawn(ExplosionSystem explosion, Instance instance, Point tntBlock) {
        MechanicsWorld world = MechanicsWorld.of(instance);
        return PrimedTnt.spawn(explosion, world, tntBlock, TntConfigResolver.resolve(config(),
                null, world, TntPrimeEvent.Cause.API, Polyp.getInstance().services()));
    }
}
