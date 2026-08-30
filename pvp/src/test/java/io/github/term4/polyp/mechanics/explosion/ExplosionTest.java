package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.api.event.explosion.ExplosionEvent;
import io.github.term4.polyp.presets.vanilla18.Explosion;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Explosion falloff: the calculator returns {@code null} past {@code 2·power}, and closer entities are hit harder. */
class ExplosionTest extends HeadlessServerTest {

    @Test
    void calculatorFallsOffWithDistance() {
        Point center = new Vec(0, 0, 0);
        ExplosionCalculator.Hit near = ExplosionCalculator.compute(center, 4f, new Vec(1, 0, 0), 1.0, 1.0f, 8.0, true, 1.0);
        ExplosionCalculator.Hit far = ExplosionCalculator.compute(center, 4f, new Vec(5, 0, 0), 5.0, 1.0f, 8.0, true, 1.0);
        assertNotNull(near);
        assertNotNull(far);
        assertTrue(near.knockback().length() > far.knockback().length(), "closer knockback should be stronger");
        assertTrue(near.damage() > far.damage(), "closer damage should be higher");
        // outside 2*power (8) -> no hit
        assertNull(ExplosionCalculator.compute(center, 4f, new Vec(9, 0, 0), 9.0, 1.0f, 8.0, true, 1.0));
    }

    @Test
    void explosionKnocksAndDamagesByDistance() {
        Point center = new Vec(2, 66, 2); // above the surface so the exposure rays clear the ground
        LivingEntity near = zombie(new Pos(2, 64, 3));
        LivingEntity far = zombie(new Pos(2, 64, 7));
        near.setHealth(20f);
        far.setHealth(20f);

        List<ExplosionEvent.Target> captured = new ArrayList<>();
        EventNode<Event> node = EventNode.all("test:explosion");
        node.addListener(ExplosionEvent.class, e -> captured.addAll(e.targets()));
        MinecraftServer.getGlobalEventHandler().addChild(node);
        try {
            // zombies stand in for players; explosion push is Players-only by default, so opt them in as KB targets
            // block breaking off: this measures damage/KB, and the harness instance is shared with other suites
            var config = Explosion.config().toBuilder().knockbackTargets(e -> true).blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build();
            new ExplosionSystem(polyp, config).explode(instance, center, 4.0f);
        } finally {
            MinecraftServer.getGlobalEventHandler().removeChild(node);
        }

        ExplosionEvent.Target tNear = find(captured, near);
        ExplosionEvent.Target tFar = find(captured, far);
        assertNotNull(tNear, "near zombie should be an explosion target");
        assertNotNull(tFar, "far zombie should be an explosion target");
        assertNotNull(tNear.knockback());
        assertNotNull(tFar.knockback());
        assertTrue(tNear.knockback().length() > tFar.knockback().length(), "closer knockback should be stronger");
        assertTrue(tNear.damage() > tFar.damage(), "closer damage should be higher");
        assertTrue(near.getHealth() < 20f, "near zombie should have taken damage");
        assertTrue(far.getHealth() < 20f, "far zombie should have taken damage");
    }

    private static ExplosionEvent.Target find(List<ExplosionEvent.Target> targets, LivingEntity entity) {
        return targets.stream().filter(t -> t.entity() == entity).findFirst().orElse(null);
    }
}
