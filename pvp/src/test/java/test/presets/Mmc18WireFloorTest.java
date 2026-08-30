package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mmc18 {@code wireMotYFloor}: every broadcast velocity's vy snaps to |vy| >= 0.05 (sign kept, exactly-flat up)
 * while the sim flies the true arc - capture 2026-07-06 (wire +-400 shorts on launch, tick-1 AND corrections;
 * an unclamped sim would send -636 on tick 1 after a -400 launch, MineMen sends -400).
 */
class Mmc18WireFloorTest extends HeadlessServerTest {

    private static ProjectileEntity launch(ProjectileConfig config, float pitch) {
        LivingEntity shooter = looseZombie();
        // y 200: clear of other tests' leftovers in the shared instance (a stray entity eats the throw on tick 1)
        shooter.setInstance(instance, new Pos(8.5, 200, 8.5, 0.0f, pitch)).join();
        var snap = ProjectileSnapshot.of(shooter, Snowball.INSTANCE).withConfig(config);
        ProjectileEntity e = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(e);
        awaitSpawn(e);
        shooter.remove();
        return e;
    }

    @Test
    void flatThrowWireFloorsUpSimStaysTrue() {
        var viewer = FakePlayer.connect(instance, new Pos(4.5, 200, 4.5), "WireFloor");
        viewer.sent.clear();
        ProjectileEntity e = launch(Projectiles.config(), 0.0f);
        assertEquals(0.0, e.velocityBt().y(), 1e-12, "sim launch unclamped");

        SpawnEntityPacket spawn = (SpawnEntityPacket) viewer.packetsFor(e.getEntityId()).getFirst();
        assertEquals(0.05, spawn.velocity().y(), 1e-9, "spawn wire vy floored up");

        e.tick(50L); // tick-1 true vy = -0.03 -> wire floors DOWN (MineMen's second spawn-flush velocity)
        var vels = viewer.packetsFor(e.getEntityId()).stream()
                .filter(p -> p instanceof EntityVelocityPacket).map(p -> (EntityVelocityPacket) p).toList();
        assertTrue(!vels.isEmpty(), "m=0 velocity sent");
        assertEquals(-0.05, vels.getLast().velocity().y(), 1e-9);
        assertEquals(-0.03, e.velocityBt().y(), 1e-9, "sim keeps the true arc");

        viewer.player.remove();
        e.remove();
    }

    @Test
    void steeperAimIsUnclamped() {
        ProjectileEntity e = launch(Projectiles.config(), 10.0f);
        assertEquals(-Math.sin(Math.toRadians(10.0f)) * 1.5, e.velocityBt().y(), 1e-9);
        e.remove();
    }
}
