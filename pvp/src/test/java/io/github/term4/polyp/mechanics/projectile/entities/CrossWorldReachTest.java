package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Stale cross-world reach-backs: a pearl/rod relationship established in one world never yanks across worlds. */
class CrossWorldReachTest extends HeadlessServerTest {

    private static PearlEntity pearlAt(LivingEntity shooter) {
        PearlEntity pearl = new PearlEntity(shooter, EntityType.ENDER_PEARL,
                ProjectileSnapshot.of(shooter, Pearl.INSTANCE), ProjectileTypeConfig.builder().build());
        pearl.setInstance(instance, new Pos(10.5, 65, 10.5)).join();
        return pearl;
    }

    private static FishingBobberEntity bobberAt(LivingEntity angler) {
        FishingBobberEntity bobber = new FishingBobberEntity(angler, EntityType.FISHING_BOBBER,
                ProjectileSnapshot.of(angler, FishingBobber.INSTANCE), ProjectileTypeConfig.builder().build());
        bobber.setInstance(instance, new Pos(9.5, 65, 9.5)).join();
        return bobber;
    }

    @Test
    void pearlDoesNotTeleportAShooterWhoLeftItsWorld() throws InterruptedException {
        LivingEntity shooter = zombie(new Pos(5.5, 65, 5.5));
        PearlEntity pearl = pearlAt(shooter);
        pearl.setTag(MechanicsWorld.ENTITY_TAG, MechanicsWorld.of(instance)); // pearl in a game world, shooter unbound
        try {
            pearl.onImpact(null);
            Thread.sleep(100);
            assertEquals(5.5, shooter.getPosition().x(), 1e-9, "no cross-world yank");
        } finally {
            pearl.remove();
            shooter.remove();
        }
    }

    @Test
    void pearlTeleportsWithinItsWorld() {
        LivingEntity shooter = zombie(new Pos(5.5, 65, 5.5));
        PearlEntity pearl = pearlAt(shooter);
        try {
            pearl.onImpact(null);
            long deadline = System.currentTimeMillis() + 2000;
            while (shooter.getPosition().x() != 10.5 && System.currentTimeMillis() < deadline) Thread.onSpinWait();
            assertEquals(10.5, shooter.getPosition().x(), 1e-9, "same world: vanilla teleport");
        } finally {
            pearl.remove();
            shooter.remove();
        }
    }

    @Test
    void reelReleasesAHookedTargetThatLeftTheWorld() {
        LivingEntity angler = zombie(new Pos(5.5, 65, 5.5));
        FishingBobberEntity bobber = bobberAt(angler);
        LivingEntity hooked = zombie(new Pos(9.5, 65, 9.5));
        hooked.setTag(MechanicsWorld.ENTITY_TAG, MechanicsWorld.of(instance)); // hooked entity now in a game world
        bobber.setHookedEntity(hooked);
        try {
            bobber.retrieve();
            assertEquals(Vec.ZERO, hooked.getVelocity(), "the line slipped: no cross-world pull");
        } finally {
            hooked.remove();
            angler.remove();
        }
    }

    @Test
    void reelPullsWithinItsWorld() {
        LivingEntity angler = zombie(new Pos(5.5, 65, 5.5));
        FishingBobberEntity bobber = bobberAt(angler);
        LivingEntity hooked = zombie(new Pos(9.5, 65, 9.5));
        bobber.setHookedEntity(hooked);
        try {
            bobber.retrieve();
            assertTrue(hooked.getVelocity().lengthSquared() > 0, "same world: the reel pulls");
        } finally {
            hooked.remove();
            angler.remove();
        }
    }
}
