package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vanilla: even a 0-damage thrown hit goes through damageEntity - hurt flash + the invul window that gates further hits. */
class ProjectileInvulTest extends HeadlessServerTest {

    private static ProjectileEntity snowballAt(LivingEntity shooter, ProjectileConfig config) {
        var snap = ProjectileSnapshot.of(shooter, Snowball.INSTANCE).withConfig(config);
        ProjectileEntity ball = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
        assertNotNull(ball);
        awaitSpawn(ball);
        return ball;
    }

    private static void tickUntilImpact(ProjectileEntity ball) {
        for (int tick = 1; tick <= 10 && !ball.isRemoved(); tick++) ball.tick(tick * 50L);
    }

    @Test
    void thrownHitOpensTheInvulWindowOnPlayers() {
        LivingEntity shooter = zombie(new Pos(12.5, 64, 9.5, 0.0f, 0.0f));
        var victim = FakePlayer.connect(instance,
                new Pos(12.5, 64, 13.5), "InvulVictim");
        ProjectileEntity ball = snowballAt(shooter, Vanilla18.projectiles());
        try {
            tickUntilImpact(ball);
            assertTrue(ball.isRemoved(), "snowball hit the player");
            assertTrue(DamageSystem.isInvulnerableToDamage(victim.player), "a thrown hit opens the player's i-frame window");
        } finally {
            if (!ball.isRemoved()) ball.remove();
            shooter.remove();
            victim.player.remove();
        }
    }

    @Test
    void mmc18ThrownHitOpensTheInvulWindow() {
        LivingEntity shooter = zombie(new Pos(9.5, 64, 9.5, 0.0f, 0.0f));
        var victim = FakePlayer.connect(instance,
                new Pos(9.5, 64, 13.5), "Mmc18Victim");
        ProjectileEntity ball = snowballAt(shooter, Projectiles.config());
        try {
            tickUntilImpact(ball);
            assertTrue(ball.isRemoved(), "mmc18 snowball hit the player");
            assertTrue(DamageSystem.isInvulnerableToDamage(victim.player), "the mmc18 snowball inherits the thrown invul window");
        } finally {
            if (!ball.isRemoved()) ball.remove();
            shooter.remove();
            victim.player.remove();
        }
    }

    @Test
    void thrownHitOpensTheInvulWindow() {
        LivingEntity shooter = zombie(new Pos(12.5, 64, 2.5, 0.0f, 0.0f));
        LivingEntity victim = zombie(new Pos(12.5, 64, 6.5));
        ProjectileEntity ball = snowballAt(shooter, Vanilla18.projectiles());
        try {
            tickUntilImpact(ball);
            assertTrue(ball.isRemoved(), "snowball hit the victim");
            assertTrue(DamageSystem.isInvulnerableToDamage(victim), "a thrown hit opens the i-frame window");

            // a second thrown hit inside the window can't beat the 0-damage highwater
            var second = services.damage().apply(DamageSnapshot.of(victim, ProjectileDamage.INSTANCE)
                    .withSource(shooter).withAmount(0f));
            assertEquals(DamageSystem.DamageOutcome.BLOCKED, second, "window absorbs the repeat hit");
        } finally {
            if (!ball.isRemoved()) ball.remove();
            shooter.remove();
            victim.remove();
        }
    }
}
