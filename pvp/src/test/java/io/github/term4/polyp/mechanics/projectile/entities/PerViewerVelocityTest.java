package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickScalingConfig;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A dilated scope re-rates the wire velocity ×20/clientTps so a slower-ticking modern participant tracks; a 1.8
 * client ticks at the server rate and would fly that many times too fast, so the send + position pin split per viewer.
 */
class PerViewerVelocityTest extends HeadlessServerTest {

    @Test
    void slowMotionSplitsWireVelocityByViewerGeneration() {
        // FakePlayers can only join the shared, warm instance, so dilate it here and restore in finally
        polyp.profiles().setInstance(instance, MechanicsKeys.TICK_SCALING, TickScalingConfig.simulated(5)); // s = 0.25
        try {
            FakePlayer legacy = FakePlayer.connect(instance, new Pos(8, 65, 8), "LegacyProjViewer");
            FakePlayer modern = FakePlayer.connect(instance, new Pos(8, 65, 8), "ModernProjViewer");
            polyp.clientInfo().setProxyDetails(legacy.player, "{\"version\":47}"); // 1.8 protocol

            ProjectileEntity ball = launch(Vanilla18.projectiles(), new Pos(8, 90, 8, -90, 0)); // throws +x, airborne

            // horizontal magnitude only - the |motY| floor (0 for vanilla18) would otherwise muddy the ratio
            double raw = hMag(ball.velocityBt());
            double legacyVel = hMag(ball.wireVelocityFor(legacy.player));
            double modernVel = hMag(ball.wireVelocityFor(modern.player));

            assertEquals(raw, legacyVel, 1e-6, "1.8 viewer gets the raw per-server-tick step (ticks at 20/s like the server)");
            assertEquals(raw * 4, modernVel, 1e-6, "modern viewer gets it re-rated x20/clientTps for its slower tick");
            ball.remove();
        } finally {
            polyp.profiles().setInstance(instance, MechanicsKeys.TICK_SCALING, null);
        }
    }

    /**
     * A 1.8 client runs native local physics on the dilated projectile, so between the sparse (~20t) position syncs
     * its arc snaps. It is pinned to the server position EVERY tick instead; a modern viewer keeps the sparse sync.
     */
    @Test
    void slowMotionPinsLegacyViewersEveryTick() {
        polyp.profiles().setInstance(instance, MechanicsKeys.TICK_SCALING, TickScalingConfig.simulated(5));
        try {
            FakePlayer legacy = FakePlayer.connect(instance, new Pos(8, 90, 8), "LegacyPinViewer");
            FakePlayer modern = FakePlayer.connect(instance, new Pos(8, 90, 8), "ModernPinViewer");
            polyp.clientInfo().setProxyDetails(legacy.player, "{\"version\":47}");

            ProjectileEntity ball = launch(Vanilla18.projectiles(), new Pos(8, 92, 8, -90, 0)); // near the viewers, airborne
            assertTrue(ball.getViewers().contains(legacy.player) && ball.getViewers().contains(modern.player), "both view the ball");
            int legacyBefore = teleports(legacy, ball), modernBefore = teleports(modern, ball);
            for (int t = 0; t < 6 && !ball.isRemoved(); t++) ball.tick(0);

            int legacyTp = teleports(legacy, ball) - legacyBefore, modernTp = teleports(modern, ball) - modernBefore;
            assertTrue(legacyTp >= 5, "legacy pinned ~every tick, was " + legacyTp);
            assertTrue(modernTp <= 2, "modern keeps the sparse sync, was " + modernTp);
            ball.remove();
        } finally {
            polyp.profiles().setInstance(instance, MechanicsKeys.TICK_SCALING, null);
        }
    }

    private static int teleports(FakePlayer fp, ProjectileEntity e) {
        return fp.sent(net.minestom.server.network.packet.server.play.EntityTeleportPacket.class).stream()
                .filter(p -> p.entityId() == e.getEntityId()).mapToInt(p -> 1).sum();
    }

    private static double hMag(net.minestom.server.coordinate.Vec v) {
        return Math.hypot(v.x(), v.z());
    }

    private static ProjectileEntity launch(ProjectileConfig config, Pos from) {
        LivingEntity shooter = looseZombie();
        shooter.setInstance(instance, from).join();
        var snap = ProjectileSnapshot.of(shooter, Snowball.INSTANCE).withConfig(config);
        ProjectileEntity entity = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        awaitSpawn(entity);
        shooter.remove();
        return entity;
    }
}
