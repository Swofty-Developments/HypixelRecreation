package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.entities.FireballEntity;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.entities.PearlEntity;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

/**
 * Hypixel projectiles: the 1.8 throwables/bow ({@link io.github.term4.polyp.presets.vanilla18.Projectiles})
 * plus the measured BedWars fireball ({@code bwFireball}: a {@code FIRE_CHARGE}, no gravity, power 2, detonating a POINT
 * on contact). It coasts at {@link #BW_LAUNCH} for {@link #BW_LAUNCH_TICKS} ticks then snaps to {@link #BW_CRUISE} - the
 * one profile that byte-fits BOTH the straight-down 1.645 KB and the ~53&deg; point-blank window.
 */
public final class Projectiles {

    private Projectiles() {}

    // first-tick move: sets the point-blank yaw window (~53 deg) and the close-range block-break offset
    private static final double BW_LAUNCH = 0.5;
    // capture wire 8359; the ramp then rides the vanilla (v+0.1)*0.95 propulsion
    private static final double BW_CRUISE = 1.0449;
    // byte-forced: two 0.5 ticks put a straight-down detonation at feet+0.62 = the exact 1.645 KB
    // (1-tick hold -> 1.781, raw propulsion ramp -> 1.6625)
    private static final int BW_LAUNCH_TICKS = 2;
    // measured radius; vanilla ghast = 1
    private static final double BW_POWER = 2.0;

    // detonate a tick late so the direct projectile KB lands before the blast; overrides FireballEntity's same-tick default
    private static final ProjectileBehavior BW_FIREBALL_DETONATION = new ProjectileBehavior() {
        @Override public void onImpact(ManagedProjectile p, Entity hit) {
            if (p instanceof FireballEntity fb) {
                Instance inst = fb.getInstance();
                Point center = fb.getPosition();
                if (inst != null) inst.scheduleNextTick(in -> fb.detonate(in, center, hit));
            }
        }
    };

    // BedWars pearl teleport, captured 2026-07-27 (130 data points)
    private static final ProjectileBehavior BW_PEARL_TELEPORT = new ProjectileBehavior() {
        @Override public void onImpact(ManagedProjectile p, Entity hit) {
            if (p instanceof PearlEntity pearl) {
                Point at = pearl.impactPosition() != null ? pearl.impactPosition() : pearl.getPosition();
                pearl.teleportShooter(new Pos(Math.floor(at.x()) + 0.5, Math.floor(at.y()) + 1, Math.floor(at.z()) + 0.5));
            }
        }
    };

    public static ProjectileConfig config() {
        ProjectileConfig base = Vanilla18.projectiles();
        ProjectileTypeConfig bwFireball = ProjectileTypeConfig.builder(Fireball.KEY)
                .boundingBox(1, 1, 1) // EntityFireball.setSize(1,1): the box OTHERS hit to deflect it; its own detonation stays a point
                .gravity(0.0).horizontalDrag(0.95).verticalDrag(0.95)
                .speed(BW_LAUNCH).coastTicks(BW_LAUNCH_TICKS).cruiseSpeed(BW_CRUISE)
                .spread(0.0)
                .spawnOffsetForward(0.0).spawnOffsetVertical(0.0).spawnOffsetSideways(0.0) // at the eye
                .leftOwnerImmunity(true)
                .syncInterval(0).velocitySyncInterval(1) // no position teleports (Hypixel/minemen don't): pure velocity prediction, no spurious 0-velocity corrections
                .removeOnEntityHit(true).removeOnBlockHit(true)
                .selfHit(ProjectileTypeConfig.HitResponse.PASS_THROUGH) // your own fireball never hits you; a deflect reassigns ownership
                .damage(0.0)
                .explosionPower(BW_POWER)
                .invulnHit(ProjectileTypeConfig.HitResponse.DESTROY)
                .behavior(BW_FIREBALL_DETONATION)
                .build();
        // rebased on vanilla18's pearl
        ProjectileTypeConfig bwPearl = ProjectileTypeConfig.builder(base.typeConfig(Pearl.KEY))
                .boundingBox(0.1, 0.1, 0.1)
                .behavior(BW_PEARL_TELEPORT)
                .teleportDamage(0.0) // BedWars pearls deal none; SkyWars keep vanilla 5 - per-mode configs differ in knobs
                .build();
        return ProjectileConfig.builder(base)
                .typeConfigs(bwFireball, bwPearl)
                .build();
    }
}
