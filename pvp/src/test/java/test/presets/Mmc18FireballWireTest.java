package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmc18 fireball wire, from the captured sessions (mmcfbdflct1 + 3 older, 139 fireballs): MineMen tracks a
 * fireball on the vanilla 10-tick cadence - absolute teleports only, and never an {@code entity_velocity}
 * (the 1.8 tracker sends velocity only when {@code velocityChanged}, which a fireball never sets).
 */
class Mmc18FireballWireTest extends HeadlessServerTest {

    @Test
    void fireballTeleportsOnTheTrackerCadenceAndSendsNoVelocity() {
        ProjectileConfig config = Projectiles.config();
        var viewer = FakePlayer.connect(instance, new Pos(4.5, 210, 4.5), "FbWire");
        LivingEntity shooter = looseZombie();
        shooter.setInstance(instance, new Pos(8.5, 210, 8.5, 0.0f, 0.0f)).join();
        try {
            var snap = ProjectileSnapshot.of(shooter, Fireball.INSTANCE).withConfig(config);
            ProjectileEntity fb = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
            assertNotNull(fb);
            awaitSpawn(fb);
            shooter.remove();
            viewer.sent.clear();

            // 1.8 increments updateCounter after the check, so counter 0 sends: the first correction lands the
            // tick after spawn. A fireball that hits a wall in ~6 ticks gets no other chance to be corrected.
            fb.tick(50L);
            assertEquals(1, viewer.packetsFor(fb.getEntityId()).stream()
                    .filter(p -> p instanceof EntityTeleportPacket).count(), "first teleport on the tick after spawn");

            for (int tick = 2; tick <= 40 && !fb.isRemoved(); tick++) fb.tick(tick * 50L);

            var mine = viewer.packetsFor(fb.getEntityId());
            long velocity = mine.stream().filter(p -> p instanceof EntityVelocityPacket).count();
            long teleports = mine.stream().filter(p -> p instanceof EntityTeleportPacket).count();
            assertTrue(velocity == 0, "a fireball never broadcasts velocity in flight, got " + velocity);
            assertEquals(4, teleports, "then the 10-tick cadence: ticks 1, 11, 21, 31");
            if (!fb.isRemoved()) fb.remove();
        } finally {
            viewer.player.remove();
        }
    }
}
