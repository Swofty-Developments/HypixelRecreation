package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hypixel/MineMen: your own fireball NEVER hits you - it passes through (selfHit PASS_THROUGH). A deflect
 * reassigns ownership, so the deflector is protected instead and the original shooter becomes a normal target.
 */
class FireballSelfHitTest extends HeadlessServerTest {

    private static ProjectileEntity launch(FakePlayer shooter, ProjectileConfig config) {
        var snap = ProjectileSnapshot.of(shooter.player, Fireball.INSTANCE).withConfig(config);
        ProjectileEntity fb = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(fb);
        awaitSpawn(fb);
        return fb;
    }

    @Test
    void ownFireballPassesThroughTheShooter() {
        record Preset(String name, ProjectileConfig config) {}
        var presets = new Preset[]{
                new Preset("mmc18", io.github.term4.polyp.presets.mmc18.Projectiles.config()),
                new Preset("hypixel", io.github.term4.polyp.presets.hypixel.Projectiles.config())};
        double x = 600.5;
        for (Preset preset : presets) {
            Pos from = new Pos(x, 100, 10.5, 0f, 0f); // +z, level flight
            FakePlayer shooter = FakePlayer.connect(instance, from, "FbSelf" + preset.name());
            try {
                ProjectileEntity fb = launch(shooter, preset.config());
                for (int tick = 1; tick <= 5; tick++) fb.tick(tick * 50L); // well clear of the launch box
                // step back into the path, at flight height
                shooter.player.teleport(new Pos(x, fb.getPosition().y() - 1.0, 18.5, 0f, 0f)).join();
                for (int tick = 6; tick <= 20 && !fb.isRemoved(); tick++) fb.tick(tick * 50L);
                assertFalse(fb.isRemoved(), preset.name() + ": passes through, no detonation");
                assertTrue(fb.getPosition().z() > 20, preset.name() + ": kept flying: " + fb.getPosition());
                assertEquals(20f, shooter.player.getHealth(), preset.name() + ": shooter untouched");
                fb.remove();
            } finally {
                shooter.player.remove();
            }
            x += 10;
        }
    }

    @Test
    void deflectReassignsOwnershipAndHitsTheOriginalShooter() {
        Pos from = new Pos(640.5, 100, 10.5, 0f, 0f); // +z
        FakePlayer shooter = FakePlayer.connect(instance, from, "FbOwnerA");
        FakePlayer deflector = FakePlayer.connect(instance, new Pos(645.5, 100, 20.5, 180f, 0f), "FbOwnerB"); // faces -z
        try {
            FireballEntity fb = (FireballEntity) launch(shooter, io.github.term4.polyp.presets.mmc18.Projectiles.config());
            for (int tick = 1; tick <= 5; tick++) fb.tick(tick * 50L);
            assertFalse(fb.deflectBy(shooter.player), "the owner can never deflect its own fireball");
            assertTrue(fb.deflectBy(deflector.player), "someone else can");
            // flies back along the deflector's look (-z) through the original shooter, who is now a normal target
            for (int tick = 6; tick <= 30 && !fb.isRemoved(); tick++) fb.tick(tick * 50L);
            assertTrue(fb.isRemoved(), "hits the original shooter: " + fb.getPosition());
            assertTrue(shooter.player.getHealth() < 20f, "contact damage landed");
        } finally {
            shooter.player.remove();
            deflector.player.remove();
        }
    }
}
