package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import net.kyori.adventure.nbt.CompoundBinaryTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Launched projectiles carry {@link MechanicsWorld#ENTITY_COPY}: the copy re-runs the launch stamp with the CURRENT velocity. */
class ProjectileCopyTest extends HeadlessServerTest {

    private static LivingEntity shooterAt(Pos pos) {
        LivingEntity shooter = looseZombie();
        shooter.setInstance(instance, pos).join();
        return shooter;
    }

    @Test
    void launchedProjectileCopiesMidFlight() {
        LivingEntity shooter = shooterAt(new Pos(8.5, 220, 8.5, 0.0f, 0.0f));
        var config = Vanilla18.projectiles();
        ProjectileEntity snowball = new ProjectileSystem(Polyp.getInstance(), config)
                .launch(ProjectileSnapshot.of(shooter, Snowball.INSTANCE).withConfig(config));
        assertNotNull(snowball);
        awaitSpawn(snowball);
        try {
            snowball.setVelocityBt(new Vec(0.3, -0.1, 0.0)); // mid-flight state; the copy must carry THIS, not the launch velocity
            Supplier<Entity> copySupplier = snowball.getTag(MechanicsWorld.ENTITY_COPY);
            assertNotNull(copySupplier, "launched projectiles are copy-ready");
            ProjectileEntity copy = (ProjectileEntity) copySupplier.get();
            assertNotSame(snowball, copy);
            assertEquals(snowball.getEntityType(), copy.getEntityType());
            assertEquals(new Vec(0.3, -0.1, 0.0), copy.velocityBt(), "current velocity, not the launch one");
            assertSame(snowball.getShooter(), copy.getShooter());
        } finally {
            snowball.remove();
            shooter.remove();
        }
    }

    @Test
    void launchedProjectileSavesAndRevives() {
        LivingEntity shooter = shooterAt(new Pos(12.5, 220, 12.5, 0.0f, 0.0f));
        var config = Vanilla18.projectiles();
        ProjectileSystem system = new ProjectileSystem(Polyp.getInstance(), config);
        ProjectileEntity snowball = system.launch(ProjectileSnapshot.of(shooter, Snowball.INSTANCE).withConfig(config));
        assertNotNull(snowball);
        awaitSpawn(snowball);
        try {
            snowball.setVelocityBt(new Vec(0.3, -0.1, 0.0));
            Supplier<CompoundBinaryTag> save = snowball.getTag(MechanicsWorld.ENTITY_SAVE);
            assertNotNull(save, "launched projectiles are save-ready");

            ProjectileEntity revived = system.fromSave(save.get());
            assertNotNull(revived);
            assertEquals(snowball.getEntityType(), revived.getEntityType());
            assertEquals(new Vec(0.3, -0.1, 0.0), revived.velocityBt(), "the saved (current) velocity, not the launch one");
            assertNull(revived.getShooter(), "only ONLINE PLAYER shooters re-link; a mob shooter doesn't");
            assertNotNull(revived.getTag(MechanicsWorld.ENTITY_SAVE), "revived projectiles save again");
            assertNotNull(revived.getTag(MechanicsWorld.ENTITY_COPY), "and fork again");
        } finally {
            snowball.remove();
            shooter.remove();
        }
    }

    @Test
    void bobbersHaveNoDefaultCopy() {
        LivingEntity shooter = shooterAt(new Pos(8.5, 220, 8.5, 0.0f, 0.0f));
        var config = Vanilla18.projectiles();
        ProjectileEntity bobber = new ProjectileSystem(Polyp.getInstance(), config)
                .launch(ProjectileSnapshot.of(shooter, FishingBobber.INSTANCE).withConfig(config));
        assertNotNull(bobber);
        awaitSpawn(bobber);
        try {
            assertNull(bobber.getTag(MechanicsWorld.ENTITY_COPY), "a forked bobber is an orphan; games stamp their own for re-attach");
            assertNull(bobber.getTag(MechanicsWorld.ENTITY_SAVE), "same for saves");
        } finally {
            bobber.remove();
            shooter.remove();
        }
    }
}
