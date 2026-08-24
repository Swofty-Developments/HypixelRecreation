package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;
import io.github.term4.polyp.mechanics.explosion.RadialScale;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.instance.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hypixel explosion: the 1.8 baseline ({@link io.github.term4.polyp.presets.vanilla18.Explosion}) plus a
 * constant radial base toward {@code feet+1} (magnitude 0.8 up, 0.8× horizontal, 0.4× downward). Damage is a flat 2.0
 * (measured: fireball + TNT both deal 2.0 regardless of distance) that ignores armor POINTS but still respects
 * enchants/effects - not the 1.8 falloff curve. Constants fitted on 240+ KB captures.
 */
public final class Explosion {

    private Explosion() {}

    private static final double BASE = 0.8;
    private static final double BASE_DOWNWARD_SCALE = 0.4;
    // capture: a block-ahead throw pushes back 0.64 horizontal (0.8× base), not the 0.8 the up-axis uses
    private static final double BASE_HORIZONTAL_SCALE = 0.8;
    private static final double BASE_HEIGHT = 1.0;
    // weaker impacts deal no explosion KB - only the projectile KB lands
    private static final double KB_IMPACT_FLOOR = 0.435;
    private static final double FLAT_DAMAGE = 2.0;

    /** Full glass blocks only - plain, stained and tinted; keyed so new variants are picked up, panes excluded. */
    private static final Set<Block> BLAST_PROOF_GLASS = Block.values().stream()
            .filter(b -> b.key().value().endsWith("glass"))
            .collect(Collectors.toUnmodifiableSet());

    // capture-verified (docs/HANDOFF): rays are 100% vanilla - glass is passable (0.3), so a block BEHIND
    // glass is still selected; it survives only if the straight line from the blast centre to it crosses
    // glass. The rule vetoes glass, OCCLUSION makes vetoed blocks cast that hard shadow. Obsidian (1200)
    // just stops rays as in vanilla and, being breakable, casts no shadow.
    private static final BlockBreaking BLOCK_BREAKING =
            io.github.term4.polyp.presets.vanilla18.Explosion.blockBreaking().toBuilder()
                    .breakRule(BlockBreaking.BreakRule.neverBreaks(BLAST_PROOF_GLASS))
                    .shielding(BlockBreaking.Shielding.OCCLUSION)
                    .build();

    /** Vanilla18 rays + blast-proof glass. */
    public static BlockBreaking blockBreaking() { return BLOCK_BREAKING; }

    public static ExplosionConfig config() {
        return ExplosionConfig.builder(Vanilla18.explosion())
                .baseKnockback(BASE).baseHeight(BASE_HEIGHT)
                .baseScale(RadialScale.builder().down(BASE_DOWNWARD_SCALE).horizontal(BASE_HORIZONTAL_SCALE).build())
                .knockbackImpactFloor(KB_IMPACT_FLOOR)
                .flatDamage(FLAT_DAMAGE).damageBypass(Bypass.builder().armor(true).build())
                .exposure(ExplosionExposure.Rays.LEGACY_1_8_FULL_CUBE) // Hypixel gates off-flat blasts (full-cube), unlike singleplayer 1.8
                // Hypixel fireballs light fire ONLY where a block was broken - never on intact ground (observed in-game)
                .fireScope(ExplosionConfig.FireScope.BROKEN)
                .blockBreaking(BLOCK_BREAKING)
                .build();
    }
}
