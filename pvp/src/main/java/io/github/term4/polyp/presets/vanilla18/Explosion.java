package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;

/** Vanilla 1.8 explosion config (damage constant 8.0, floored; modern is 7.0 unfloored); also the {@code ExplosionSystem} fallback. */
public final class Explosion {

    private Explosion() {}

    private static final BlockBreaking BLOCK_BREAKING = BlockBreaking.builder()
            .model(BlockBreaking.Model.RAY_1_8)
            .resistance(BlockBreaking.LEGACY_RESISTANCE)
            .interaction(BlockBreaking.Interaction.DESTROY_WITH_DECAY)
            .build();

    /** The 1.8 block policy on its own, for presets that re-base it. */
    public static BlockBreaking blockBreaking() { return BLOCK_BREAKING; }

    public static ExplosionConfig config() {
        return ExplosionConfig.builder()
                .power(4.0) // TNT default radius
                .damageConstant(8.0)
                .floorDamage(true)
                .knockbackMultiplier(1.0)
                .exposure(ExplosionExposure.Rays.LEGACY_1_8) // faithful 1.8 rayTraceBlocks (block-real shape, pushes off-flat); servers override to LEGACY_1_8_FULL_CUBE
                // vanilla gates this per EXPLODER, not per world: a fireball is incendiary, TNT never is
                // (1.8 EntityLargeFireball passes its mobGriefing flag as BOTH fire and blockDamage; 26.1
                // LargeFireball passes it as the fire arg). polyp has no gamerules, so the split lives here.
                .fire(ctx -> ctx.source() instanceof FireballEntity)
                .blockBreaking(BLOCK_BREAKING)
                .build();
    }
}
