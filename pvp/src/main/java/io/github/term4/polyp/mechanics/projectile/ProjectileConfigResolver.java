package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.Config;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves a {@link ProjectileConfig} + snapshot into plain values, in two phases: {@link #resolveFlight} at launch
 * (spawn + physics) and {@link #resolveHit} at impact (hit knobs, against a context with the struck target + throw
 * origin). The merged {@link ProjectileTypeConfig} is computed once at launch and reused for impact.
 */
public final class ProjectileConfigResolver {

    private ProjectileConfigResolver() {}

    /**
     * The context the per-type {@code FieldValue}s resolve against. Launch-time carries the snapshot + services;
     * {@link #atHit} adds the impact fields ({@link #target}, {@link #throwOrigin}, {@link #hitPos}).
     */
    public record ProjectileContext(ProjectileSnapshot snap, @Nullable Services services,
                                    @Nullable Entity target, @Nullable Pos throwOrigin, @Nullable Point hitPos) {
        public static ProjectileContext of(ProjectileSnapshot snap, @Nullable Services services) {
            return new ProjectileContext(snap, services, null, null, null);
        }

        /** The impact-time context for resolving the hit knobs. */
        public ProjectileContext atHit(@Nullable Entity target, @Nullable Pos throwOrigin, @Nullable Point hitPos) {
            return new ProjectileContext(snap, services, target, throwOrigin, hitPos);
        }

        public Entity shooter() { return snap.shooter(); }
        public @Nullable ItemStack item() { return snap.item(); }
        public double power() { return snap.power(); }

        /** The struck entity (impact only), or {@code null} for a block hit / at launch. */
        public @Nullable Entity target() { return target; }
        public boolean isSelfHit() { return target != null && target == snap.shooter(); }
        /** The shooter's position + view at throw time (impact only). */
        public @Nullable Pos throwOrigin() { return throwOrigin; }
        /** Impact only. */
        public @Nullable Point hitPos() { return hitPos; }

        /**
         * Effective per-type config, layered highest-first: the active config's per-type override -&gt; its generic
         * {@link ProjectileConfig#defaults()} -&gt; the type's {@code defaultConfig()}, plus any {@code subConfig} overlay.
         */
        public ProjectileTypeConfig typeConfig() {
            ProjectileConfig cfg = snap.config();
            if (cfg == null && services != null && services.projectiles() != null) cfg = services.projectiles().config();
            return Config.layer(snap.type().defaultConfig(),
                    cfg != null ? cfg.defaults() : null,
                    cfg != null ? cfg.typeConfig(snap.type().key()) : null,
                    this);
        }
    }

    /** Resolves the flight knobs (spawn + physics) at launch from the effective type config. */
    public static ResolvedFlight resolveFlight(ProjectileTypeConfig tc, ProjectileContext ctx) {
        return new ResolvedFlight(
                FieldValue.resolve(tc.enabled, ctx, Boolean.TRUE),
                FieldValue.resolve(tc.boundingBox, ctx, POINT_BOX),
                FieldValue.resolve(tc.gravity, ctx, 0.03),
                FieldValue.resolve(tc.horizontalDrag, ctx, 0.99),
                FieldValue.resolve(tc.verticalDrag, ctx, 0.99),
                FieldValue.resolve(tc.waterDrag, ctx, 0.8), // vanilla throwable/fireball; arrows override 0.6
                FieldValue.resolve(tc.waterPush, ctx, 0.014), // 1.8 Entity.W() current
                FieldValue.resolve(tc.waterModel, ctx, ProjectileTypeConfig.WaterModel.LEGACY),
                FieldValue.resolve(tc.legacyBlockRay, ctx, Boolean.FALSE), // 1.8 selection-box ray for >1-tall blocks

                FieldValue.resolve(tc.spawnOffsetForward, ctx, 0.0),
                FieldValue.resolve(tc.spawnOffsetVertical, ctx, 0.0),
                FieldValue.resolve(tc.spawnOffsetSideways, ctx, 0.0),
                FieldValue.resolve(tc.speed, ctx, 1.5),
                FieldValue.resolve(tc.launchPitchOffset, ctx, 0.0), // vanilla splash potion / XP bottle = -20
                FieldValue.resolve(tc.spread, ctx, 0.0),
                FieldValue.resolve(tc.spreadBias, ctx, 0.0), // MineMen arrows: the gaussian pinned to +1 (0.0075/axis)
                FieldValue.resolve(tc.wireMotYFloor, ctx, 0.0), // MineMen throwables: every broadcast vy snaps to |vy| >= 0.05
                FieldValue.resolve(tc.momentumHorizontal, ctx, 0.0), // vanilla 1.8 adds no shooter momentum (26.1 = 1.0)
                FieldValue.resolve(tc.momentumVertical, ctx, 0.0),
                FieldValue.resolve(tc.shooterImmunityTicks, ctx, 5),
                FieldValue.resolve(tc.entityHitGrow, ctx, 0.3), // vanilla 1.8 Entity{Arrow,Projectile}: target grow 0.3 each side
                FieldValue.resolve(tc.broadcastMovement, ctx, Boolean.FALSE), // vanilla trackers broadcast per tick; silent = the client-prediction mode
                FieldValue.resolve(tc.syncInterval, ctx, 20),
                FieldValue.resolve(tc.velocitySyncInterval, ctx, 0), // 0 = no per-tick velocity (vanilla arrow); the edge-slide fix
                FieldValue.resolve(tc.physicsOrder, ctx, ProjectileTypeConfig.PhysicsOrder.DRAG_AFTER_MOVE), // 26.1 = DRAG_BEFORE_MOVE
                FieldValue.resolve(tc.wireGrid, ctx, ProjectileTypeConfig.WireGrid.LEGACY_1_8), // which client's decoded wire silent flight snaps to
                FieldValue.resolve(tc.wireLockstep, ctx), // nullable: unset = lockstep only when there is no per-tick velocity sync
                FieldValue.resolve(tc.leftOwnerImmunity, ctx, Boolean.FALSE), // 26.1 = true (immune until it leaves the shooter box)
                FieldValue.resolve(tc.stickPullback, ctx, 0.05), // vanilla 0.05 tip poke-out
                FieldValue.resolve(tc.shakeTicks, ctx, 7), // vanilla arrow shake / pickup delay
                FieldValue.resolve(tc.explosionPower, ctx, 1.0), // vanilla ghast fireball yield (Hypixel = 2.0); fireball-only
                FieldValue.resolve(tc.coastTicks, ctx, 0), // fireball launch-coast ticks (0 = propel at once)
                FieldValue.resolve(tc.cruiseSpeed, ctx, 0.0), // fireball ignition speed after the coast (0 = none)
                FieldValue.resolve(tc.critChance, ctx, 1.0), // full-draw crit chance (vanilla = always); bow-only
                FieldValue.resolve(tc.behavior, ctx, ProjectileBehavior.NONE),
                FieldValue.resolve(tc.pickupBox, ctx)); // nullable: the entity keeps its vanilla default if unset
    }

    /** Resolves the hit knobs at impact from the effective type config against the impact {@code ctx}. */
    public static ResolvedHit resolveHit(ProjectileTypeConfig tc, ProjectileContext ctx) {
        return new ResolvedHit(
                FieldValue.resolve(tc.selfHit, ctx, ProjectileTypeConfig.HitResponse.HIT),
                FieldValue.resolve(tc.entityHit, ctx, ProjectileTypeConfig.HitResponse.HIT),
                FieldValue.resolve(tc.knockback, ctx),
                FieldValue.resolve(tc.knockbackSource, ctx, ProjectileTypeConfig.KnockbackSource.PROJECTILE),
                FieldValue.resolve(tc.damage, ctx, 0.0),
                FieldValue.resolve(tc.damageType, ctx),
                FieldValue.resolve(tc.removeOnEntityHit, ctx, Boolean.TRUE),
                FieldValue.resolve(tc.removeOnBlockHit, ctx, Boolean.TRUE),
                FieldValue.resolve(tc.invulnHit, ctx, ProjectileTypeConfig.InvulnResponse.of(ProjectileTypeConfig.HitResponse.DESTROY)), // throwables break; arrow = invulnHit(DEFLECT, PASS_THROUGH)
                FieldValue.resolve(tc.deflect, ctx, ProjectileTypeConfig.Deflect.of(-0.1))); // vanilla 1.8 motion *= -0.1; 26.1 = deflect(-0.5, 0, -10, 10)
    }

    /** Zero-size box: collision points resolve exactly on block boundaries. */
    private static final BoundingBox POINT_BOX = new BoundingBox(0, 0, 0);

    /** Flight values resolved at launch (spawn + physics). */
    public record ResolvedFlight(
            boolean enabled,
            BoundingBox boundingBox,
            double gravity,
            double horizontalDrag,
            double verticalDrag,
            double waterDrag,
            double waterPush,
            ProjectileTypeConfig.WaterModel waterModel,
            boolean legacyBlockRay,
            double spawnOffsetForward,
            double spawnOffsetVertical,
            double spawnOffsetSideways,
            double speed,
            double launchPitchOffset,
            double spread,
            double spreadBias,
            double wireMotYFloor,
            double momentumHorizontal,
            double momentumVertical,
            int shooterImmunityTicks,
            double entityHitGrow,
            boolean broadcastMovement,
            int syncInterval,
            int velocitySyncInterval,
            ProjectileTypeConfig.PhysicsOrder physicsOrder,
            ProjectileTypeConfig.WireGrid wireGrid,
            @Nullable Boolean wireLockstep,
            boolean leftOwnerImmunity,
            double stickPullback,
            int shakeTicks,
            double explosionPower,
            int coastTicks,
            double cruiseSpeed,
            double critChance,
            ProjectileBehavior behavior,
            @Nullable ProjectileTypeConfig.PickupBox pickupBox
    ) {}

    /** Hit values resolved at impact ({@code knockback}/{@code damageType} nullable). */
    public record ResolvedHit(
            ProjectileTypeConfig.HitResponse selfHit,
            ProjectileTypeConfig.HitResponse entityHit,
            @Nullable KnockbackConfig knockback,
            ProjectileTypeConfig.KnockbackSource knockbackSource,
            double damage,
            @Nullable DamageType damageType,
            boolean removeOnEntityHit,
            boolean removeOnBlockHit,
            ProjectileTypeConfig.InvulnResponse invulnHit,
            ProjectileTypeConfig.Deflect deflect
    ) {}
}
