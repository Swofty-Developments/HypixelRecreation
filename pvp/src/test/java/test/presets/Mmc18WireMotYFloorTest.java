package test.presets;

import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ProjectileContext;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.Egg;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.mechanics.projectile.types.SplashPotion;
import io.github.term4.polyp.presets.mmc18.Projectiles;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Every mmc18 projectile inherits the 0.05 wire motY floor via the config-wide {@code defaults}, not per type. */
class Mmc18WireMotYFloorTest extends HeadlessServerTest {

    private static double floor(ProjectileConfig cfg, ProjectileType type) {
        ProjectileSnapshot snap = ProjectileSnapshot.of(null, type).withConfig(cfg);
        ProjectileContext ctx = ProjectileContext.of(snap, services);
        return ProjectileConfigResolver.resolveFlight(ctx.typeConfig(), ctx).wireMotYFloor();
    }

    @Test
    void everyMmc18ProjectileFloorsWireMotY() {
        ProjectileConfig cfg = Projectiles.config();
        ProjectileType[] types = {
                Fireball.INSTANCE, SplashPotion.INSTANCE, FishingBobber.INSTANCE,
                Snowball.INSTANCE, Egg.INSTANCE, Pearl.INSTANCE, Arrow.INSTANCE};
        for (ProjectileType t : types) {
            assertEquals(0.05, floor(cfg, t), 1e-9, t.key() + " should inherit the 0.05 wire motY floor");
        }
    }
}
