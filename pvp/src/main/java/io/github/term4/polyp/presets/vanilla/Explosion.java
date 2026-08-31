package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;

/** Modern (26.1) explosion config: damage constant 7.0, unfloored (vs 1.8's 8.0, floored). */
public final class Explosion {

    private Explosion() {}

    public static ExplosionConfig config() {
        return ExplosionConfig.builder()
                .power(4.0) // TNT default radius
                .damageConstant(7.0)
                .floorDamage(false)
                .knockbackMultiplier(1.0)
                .exposure(ExplosionExposure.Rays.MODERN)
                // vanilla gates this per EXPLODER, not per world: a fireball is incendiary, TNT never is
                // (1.8 EntityLargeFireball passes its mobGriefing flag as BOTH fire and blockDamage; 26.1
                // LargeFireball passes it as the fire arg). polyp has no gamerules, so the split lives here.
                .fire(ctx -> ctx.source() instanceof FireballEntity)
                .blockBreaking(BlockBreaking.builder()
                        .model(BlockBreaking.Model.RAY_MODERN)
                        .resistance(BlockBreaking.VANILLA_RESISTANCE)
                        .interaction(BlockBreaking.Interaction.DESTROY_WITH_DECAY)
                        .build())
                .build();
    }
}
