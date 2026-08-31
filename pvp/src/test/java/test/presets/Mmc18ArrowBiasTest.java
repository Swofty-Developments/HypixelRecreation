package test.presets;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MineMen arrows are deterministic: the vanilla spread gaussian pinned to +1, i.e. +0.0075 on every direction
 * axis pre-scale (capture 2026-07-28: straight-up full draw = (+179, 24179, +179) on the wire).
 */
class Mmc18ArrowBiasTest extends HeadlessServerTest {

    @Test
    void arrowCarriesTheConstantSpreadBias() {
        FakePlayer shooter = FakePlayer.connect(instance, new Pos(620.5, 80, 620.5, 0f, -90f), "MmcArrowBias");
        try {
            ProjectileConfig config = Projectiles.config();
            var snap = ProjectileSnapshot.of(shooter.player, Arrow.INSTANCE).withConfig(config).withPower(1.0);
            ProjectileEntity arrow = new ProjectileSystem(Polyp.getInstance(), config).launch(snap);
            assertNotNull(arrow);
            awaitSpawn(arrow);
            Vec v = arrow.velocityBt();
            assertEquals(0.0225, v.x(), 2e-3, "straight up, full draw: " + v);
            assertEquals(3.0225, v.y(), 2e-3, "straight up, full draw: " + v);
            assertEquals(0.0225, v.z(), 2e-3, "straight up, full draw: " + v);
            arrow.remove();
        } finally {
            shooter.player.remove();
        }
    }
}
