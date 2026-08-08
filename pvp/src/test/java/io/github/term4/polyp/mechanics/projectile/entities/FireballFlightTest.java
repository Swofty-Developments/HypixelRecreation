package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Fireball ignition: coast at the launch speed for N ticks, then snap to cruise and ride the {@code (v+0.1)*0.95} propulsion. */
class FireballFlightTest extends HeadlessServerTest {

    private static final double LAUNCH = 0.5, CRUISE = 1.0449, PROP = 0.1, DRAG = 0.95;

    // a fireball flying +Z in open air above the floor (no collision), full 0.95 drag / no gravity
    private static FireballEntity fireball(int coastTicks, double cruise) {
        ProjectileSnapshot snap = new ProjectileSnapshot(null, Fireball.INSTANCE, null, 1.0, null, null, null, null);
        FireballEntity fb = new FireballEntity(null, EntityType.FIREBALL, snap, ProjectileTypeConfig.builder(Fireball.KEY).build());
        fb.setAerodynamics(new Aerodynamics(0.0, DRAG, DRAG));
        fb.setIgnition(coastTicks, cruise);
        fb.setInstance(instance, new Pos(0.5, 100, 0.5)).join();
        fb.setVelocityBt(new Vec(0, 0, LAUNCH));
        return fb;
    }

    // per-tick forward move = the velocity the tick actually flew (recorded as the Z displacement)
    private static double[] moves(FireballEntity fb, int ticks) {
        double[] out = new double[ticks];
        for (int i = 0; i < ticks; i++) {
            double before = fb.getPosition().z();
            fb.tick(i);
            out[i] = fb.getPosition().z() - before;
        }
        return out;
    }

    @Test
    void twoTickHoldThenCruise() {
        double[] m = moves(fireball(2, CRUISE), 4);
        assertEquals(LAUNCH, m[0], 1e-9, "tick 1 coasts at launch");
        assertEquals(LAUNCH, m[1], 1e-9, "tick 2 still coasts (the byte-fit hold -> straight-down 1.645)");
        assertEquals(CRUISE, m[2], 1e-9, "tick 3 ignites to cruise");
        assertEquals((CRUISE + PROP) * DRAG, m[3], 1e-9, "tick 4 rides propulsion");
    }

    @Test
    void oneTickHoldThenCruise() {
        double[] m = moves(fireball(1, CRUISE), 3);
        assertEquals(LAUNCH, m[0], 1e-9, "tick 1 coasts");
        assertEquals(CRUISE, m[1], 1e-9, "tick 2 ignites (mmc18 profile)");
        assertEquals((CRUISE + PROP) * DRAG, m[2], 1e-9, "tick 3 rides propulsion");
    }

    @Test
    void zeroCoastPropelsFromTickOne() {
        double[] m = moves(fireball(0, 0), 3);
        assertEquals(LAUNCH, m[0], 1e-9, "tick 1 flies the launch velocity");
        assertEquals((LAUNCH + PROP) * DRAG, m[1], 1e-9, "tick 2 already propelled (no hold, vanilla)");
        assertEquals(((LAUNCH + PROP) * DRAG + PROP) * DRAG, m[2], 1e-9, "tick 3 keeps ramping");
    }
}
