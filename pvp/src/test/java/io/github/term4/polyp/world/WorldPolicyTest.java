package io.github.term4.polyp.world;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The default gate: same binding (or none) may interact; a bound entity is out of unbound reach and vice versa. */
class WorldPolicyTest extends HeadlessServerTest {

    @Test
    void defaultGatesAcrossBindings() {
        LivingEntity a = zombie(new Pos(60, 65, 60));
        LivingEntity b = zombie(new Pos(60, 65, 62));
        try {
            assertTrue(WorldPolicy.canAffect(a, b), "unbound entities share the plain world");
            b.setTag(MechanicsWorld.ENTITY_TAG, MechanicsWorld.of(instance)); // any non-null binding differs from none
            assertFalse(WorldPolicy.canAffect(a, b), "bindings gate both directions");
            assertFalse(WorldPolicy.canAffect(b, a));
        } finally {
            a.remove();
            b.remove();
        }
    }
}
