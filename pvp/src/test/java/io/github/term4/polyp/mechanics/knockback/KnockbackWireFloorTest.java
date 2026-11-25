package io.github.term4.polyp.mechanics.knockback;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.presets.mmc18.Knockback;
import io.github.term4.polyp.presets.mmc18.Movement;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The mmc18 broadcast floor (the scope's {@code VelocityConfig.wireFloorY}): |vy| < 0.05 b/t leaves the server as
 *  sign*0.05 (0 -> +0.05) - a sign-keeping magnitude floor, never floored up. */
class KnockbackWireFloorTest extends HeadlessServerTest {

    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;
    private static Instance scoped;

    @BeforeAll
    static void scope() {
        scoped = flatInstance(MechanicsProfile.builder().set(MechanicsKeys.VELOCITY, Movement.velocity()).build());
    }

    /** Delivers {@code bt} through the mmc18 wire (values chosen on the 1/8000 grid so quantize is identity). */
    private Vec delivered(Vec bt) {
        LivingEntity victim = looseZombie();
        victim.setInstance(scoped, new Pos(0, 64, 750)).join();
        new KnockbackSystem(polyp, Knockback.melee()).deliver(victim, bt.mul(TPS));
        Vec out = victim.getVelocity().div(TPS);
        victim.remove();
        return out;
    }

    @Test
    void smallVerticalFloorsSigned() {
        assertEquals(0.05, delivered(new Vec(0.125, 0.02, 0)).y(), 1e-9, "small positive floors up");
        assertEquals(-0.05, delivered(new Vec(0.125, -0.02, 0)).y(), 1e-9, "small negative floors DOWN - sign kept");
        assertEquals(0.05, delivered(new Vec(0.125, 0, 0)).y(), 1e-9, "zero goes out as +0.05");
    }

    @Test
    void everythingElseUntouched() {
        Vec out = delivered(new Vec(0.125, 0.25, -0.5));
        assertEquals(0.25, out.y(), 1e-9);
        assertEquals(0.125, out.x(), 1e-9);
        assertEquals(-0.5, out.z(), 1e-9);
    }
}
