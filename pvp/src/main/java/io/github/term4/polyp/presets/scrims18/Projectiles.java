package io.github.term4.polyp.presets.scrims18;

import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

/**
 * Scrims 1.8 projectiles, capture-fitted (141 spawns + two held-aim sessions). Four deltas off {@link Vanilla18}:
 * a silent wire (spawn + launch velocity, never a correction - 0 teleports across every type), zero launch
 * spread (128 held-aim snowballs collapse onto 4 vectors, one per aim; the off-axis component is a hard 0),
 * a centered spawn ({@link #SPAWN_FORWARD} down the aim instead of 1.8's 0.16 sideways step), and launch
 * speeds a shade under 1.8's ({@link #THROW_SPEED}/{@link #ROD_SPEED}). Flight physics stay vanilla - with
 * no follow-up packets the captures can neither confirm nor move gravity/drag.
 */
public final class Projectiles {

    private Projectiles() {}

    // scrims18staticsnowballtest, 128 throws over 4 held aims, wire velocities +-(11962,-74,0) and
    // +-(11965,-69,0): |v| lands 1.4952-1.4959, a quantum either side of this. Paper 1.8's EntityProjectile.j()
    // returns 1.5F flat, so the 0.3% is theirs.
    private static final double THROW_SPEED = 1.4955;
    // scrims18staticrod, 40 casts from a held aim: 39 of them the same (11723,-87,0). Well under the 1.5F
    // EntityFishingHook launches with, and 0.98x the throwables - the rod is nerfed on its own, not globally.
    private static final double ROD_SPEED = 1.4655;

    // out along the view instead of 1.8's step to the side. The two static captures face opposite ways, so
    // their floor-quantized readings bracket it from both sides: 0.40625 < forward < 0.4375. 0.425 is the
    // player's half-width plus the projectile's (0.3 + 0.125) - it clears the thrower's box exactly.
    private static final double SPAWN_FORWARD = 0.425;
    // same captures: spawn y sits a wire step above 1.8's headHeight-0.1, bracketing eye-0.089 .. eye-0.058
    private static final double SPAWN_VERTICAL = -0.08;

    /** Vanilla 1.8 projectiles with the periodic wire sync turned off on every type. */
    public static ProjectileConfig config() {
        ProjectileConfig base = Vanilla18.projectiles();
        return ProjectileConfig.builder(base)
                // generic base covers snowball / egg / pearl (they inherit both knobs), and carries the no-spread
                // patch down to arrow + bobber, whose entries never set spread themselves. Arrow and splash pin
                // their own speed, so THROW_SPEED reaches only the types it was measured on.
                .defaults(silent(ProjectileTypeConfig.builder(base.defaults())
                        .spread(0.0).speed(THROW_SPEED)
                        .spawnOffsetForward(SPAWN_FORWARD)
                        .spawnOffsetSideways(0.0)
                        .spawnOffsetVertical(SPAWN_VERTICAL)))
                // arrow + bobber set their own interval, so silence them explicitly
                .typeConfigs(
                        silent(ProjectileTypeConfig.builder(base.typeConfig(Arrow.KEY))),
                        silent(ProjectileTypeConfig.builder(base.typeConfig(FishingBobber.KEY)).speed(ROD_SPEED)))
                .build();
    }

    private static ProjectileTypeConfig silent(ProjectileTypeConfig.Builder b) {
        return b.syncInterval(0).velocitySyncInterval(0).build();
    }
}
