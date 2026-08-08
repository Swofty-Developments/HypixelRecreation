package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.entities.PearlEntity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.Egg;
import io.github.term4.polyp.mechanics.projectile.types.Fireball;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.Pearl;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.mechanics.projectile.types.Snowball;
import io.github.term4.polyp.mechanics.projectile.types.SplashPotion;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * mmc18 projectiles: the 1.8 baseline plus the minemen fireball (FIRE_CHARGE, self-propelled, no gravity, power 2),
 * the silent-wire splash, and the pseudo-hook rod ({@link PseudoHook}).
 * Fireball flight measured from the 2026-07-01 MineMen flight logs (fireball_flight.py): spawn at the shot eye, first tick
 * moves {@link #LAUNCH}, then the velocity snaps to {@link #CRUISE} and rides the vanilla propulsion curve
 * ((v+0.1)&middot;0.95 -&gt; 1.0884...). A direct hit is the vanilla 6.0 CONTACT hit with the mmc18 hurt-KB away from the fireball;
 * the same-tick splash then lands in the contact's i-frame window (FBF's &times;0.05 damage = blocked, normal-mode falloff =
 * overdamage remainder + push).
 */
public final class Projectiles {

    private Projectiles() {}

    // measured: wire launch 0.5645 x drag = 0.5363 first-tick move; cruise 1.0457 b/t from tick 2
    private static final double LAUNCH = 0.5645 * 0.95;
    private static final double CRUISE = 1.0457;
    // measured radius; vanilla ghast = 1
    private static final double POWER = 2.0;
    // vanilla EntityLargeFireball 6.0, unchanged in the FBF captures
    private static final double CONTACT_DAMAGE = 6.0;
    // every minemen projectile floors |motY| to 0.05 on the wire (sim untouched); vertical-launch types (splash) just hid it
    private static final double WIRE_MOTY_FLOOR = 0.05;

    private static final Logger LOG = LoggerFactory.getLogger(Projectiles.class);
    private static final BoundingBox PLAYER_BOX = new BoundingBox(0.6, 1.8, 0.6);
    private static final double[] FREE_AXIS_STANDOFF = {-0.4, 0.4};

    // minemen pearl, capture-fitted. Wedged throwers short-circuit (boxedIn); otherwise walk back from the
    // contact to the first spot the player box fits, and refuse if there is none.
    private static final ProjectileBehavior PEARL_TELEPORT = new ProjectileBehavior() {
        @Override public void onImpact(ManagedProjectile p, Entity hit) {
            if (!(p instanceof PearlEntity pearl)) return;
            Point at = pearl.impactPosition() != null ? pearl.impactPosition() : pearl.getPosition();
            BlockFace face = pearl.impactFace();
            Pos target;
            if (hit != null) {
                Pos e = hit.getPosition();
                double dx = at.x() - e.x(), dz = at.z() - e.z();
                double m = Math.hypot(dx, dz);
                target = m > 1e-6 ? new Pos(e.x() + dx / m, e.y(), e.z() + dz / m) : new Pos(e.x(), e.y(), e.z());
            } else if (face == null || pearl.impactCell() == null || pearl.impactShape() == null) {
                target = new Pos(at.x(), at.y(), at.z());
            } else {
                target = boxedIn(pearl, face);
                if (target == null) target = walkBack(pearl, at, face);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("pearl impact {} face {} -> {}", at, face, target == null ? "REFUSED" : target);
            }
            if (target != null) pearl.teleportShooter(target);
            else pearl.consumeOnShooter();
        }
    };

    /**
     * A thrower with no room to stand a block higher skips the walk-back entirely and rises to the top of their
     * own feet cell, x/z untouched - but only while standing INSIDE something. Slabs, trapdoors and water all
     * qualify; flat ground (air at the feet, block below) does not, and mid-jump refuses for the same reason.
     */
    private static @Nullable Pos boxedIn(PearlEntity pearl, BlockFace face) {
        if (face == BlockFace.BOTTOM) return null; // a ceiling hit already resolves to plane-2, the same spot
        Entity shooter = pearl.getShooter();
        if (shooter == null) return null;
        Pos from = shooter.getPosition();
        MechanicsWorld world = MechanicsWorld.of(pearl);
        if (boxFits(world, from.x(), from.y() + 1, from.z())) return null;
        int cy = (int) Math.floor(from.y());
        if (world.getBlock((int) Math.floor(from.x()), cy, (int) Math.floor(from.z()),
                Block.Getter.Condition.TYPE).isAir()) return null;
        return new Pos(from.x(), cy + 1, from.z());
    }

    private static @Nullable Pos walkBack(PearlEntity pearl, Point at, BlockFace face) {
        // face plane from the struck cell + shape; impactPosition() clips on the contact side
        Point cell = pearl.impactCell();
        Shape shape = pearl.impactShape();
        double plane = switch (face) {
            case TOP -> cell.y() + shape.relativeEnd().y();
            case BOTTOM -> cell.y() + shape.relativeStart().y();
            case EAST -> cell.x() + shape.relativeEnd().x();
            case WEST -> cell.x() + shape.relativeStart().x();
            case SOUTH -> cell.z() + shape.relativeEnd().z();
            case NORTH -> cell.z() + shape.relativeStart().z();
        };
        Pos pre = pearl.getPosition();
        List<Pos> walk = new ArrayList<>();
        if (face == BlockFace.TOP || face == BlockFace.BOTTOM) {
            double y = face == BlockFace.TOP ? plane : plane - 2;
            walk.add(new Pos(pre.x(), y, pre.z()));
            for (Pos h : pearl.stepHistory()) walk.add(new Pos(h.x(), y, h.z()));
        } else {
            int normal = face == BlockFace.EAST || face == BlockFace.WEST
                    ? face.toDirection().normalX() : face.toDirection().normalZ();
            Pos spot = clearSpot(pre, at, face, plane + normal * 0.4);
            if (spot != null) {
                walk.add(spot);
                // the free axis takes the same 0.4 off a wall it rests against, measured from the contact.
                // Only the sign pointing away from that wall fits, so trying both is not a choice.
                boolean xAxis = face == BlockFace.EAST || face == BlockFace.WEST;
                for (double s : FREE_AXIS_STANDOFF) {
                    walk.add(xAxis ? new Pos(spot.x(), spot.y(), at.z() + s)
                                   : new Pos(at.x() + s, spot.y(), spot.z()));
                }
            }
            walk.add(pre);
            for (Pos h : pearl.stepHistory()) walk.add(h);
        }
        MechanicsWorld world = MechanicsWorld.of(pearl);
        for (Pos c : walk) {
            if (boxFits(world, c.x(), c.y(), c.z())) return c;
        }
        return null;
    }

    /** x/z extrapolated along the impact segment until the hit axis sits at {@code coord}; y at the contact
     *  (a flight-aligned y is ill-conditioned on steep hug-climbs). Null when degenerate. */
    private static @Nullable Pos clearSpot(Pos pre, Point at, BlockFace face, double coord) {
        double dx = at.x() - pre.x(), dz = at.z() - pre.z();
        if (dx * dx + dz * dz < 1e-12) return null; // clipped at the spawn (started inside)
        boolean xAxis = face == BlockFace.EAST || face == BlockFace.WEST;
        double d = xAxis ? dx : dz;
        if (Math.abs(d) < 1e-9) return null;
        double t = (coord - (xAxis ? pre.x() : pre.z())) / d;
        return new Pos(pre.x() + dx * t, at.y(), pre.z() + dz * t);
    }

    // no mmc teleport ever overlaps a collision shape; hug-distance centimetres decide teleport vs refusal.
    // Unloaded = no fit.
    private static boolean boxFits(MechanicsWorld world, double x, double y, double z) {
        int x0 = (int) Math.floor(x - 0.3), x1 = (int) Math.floor(x + 0.3);
        int z0 = (int) Math.floor(z - 0.3), z1 = (int) Math.floor(z + 0.3);
        int y0 = (int) Math.floor(y), y1 = (int) Math.ceil(y + 1.8) - 1;
        for (int cx = x0; cx <= x1; cx++)
            for (int cz = z0; cz <= z1; cz++) {
                if (!world.isChunkLoaded(cx >> 4, cz >> 4)) return false;
                for (int cy = y0; cy <= y1; cy++) {
                    Block block = world.getBlock(cx, cy, cz, Block.Getter.Condition.TYPE);
                    if (block.isAir()) continue;
                    Shape s = block.registry().collisionShape();
                    if (s != null && s.intersectBox(new Vec(x - cx, y - cy, z - cz), PLAYER_BOX)) return false;
                }
            }
        return true;
    }

    public static ProjectileConfig config() {
        ProjectileConfig base = Vanilla18.projectiles();
        ProjectileTypeConfig fireball = ProjectileTypeConfig.builder(Fireball.KEY)
                .boundingBox(1, 1, 1)
                .gravity(0.0).horizontalDrag(0.95).verticalDrag(0.95)
                .speed(LAUNCH).coastTicks(1).cruiseSpeed(CRUISE).spread(0.0) // coast one tick at launch, then ignite to cruise
                .spawnOffsetForward(0.0).spawnOffsetVertical(0.0).spawnOffsetSideways(0.0)
                .leftOwnerImmunity(true)
                // captured wire (mmcfbdflct1 + 3 older sessions, 139 fireballs): absolute teleports on the
                // vanilla 10-tick tracker cadence and NEVER an entity_velocity - the 1.8 tracker only sends
                // velocity when velocityChanged, which a fireball never sets
                .syncInterval(10).velocitySyncInterval(0)
                .removeOnEntityHit(true).removeOnBlockHit(true)
                .selfHit(ProjectileTypeConfig.HitResponse.PASS_THROUGH) // your own fireball never hits you; a deflect reassigns ownership
                .damage(CONTACT_DAMAGE)
                .knockback(Knockback.explosionHurt())
                .knockbackSource(ProjectileTypeConfig.KnockbackSource.PROJECTILE)
                .explosionPower(POWER)
                .invulnHit(ProjectileTypeConfig.HitResponse.DESTROY)
                .build(); // no behavior: the bare fireball detonates same-tick at its pre-move centre
        // capture 2026-07-06: 0.55 (not 0.5), no spread, silent flight (spawn + velocity dup only)
        ProjectileTypeConfig splash = ProjectileTypeConfig.builder(base.typeConfig(SplashPotion.KEY))
                .speed(0.55).spread(0.0)
                .syncInterval(0).velocitySyncInterval(0)
                .build();
        // rod: fully client-predicted silent wire (lockstep spawn on the 1.8 grid).
        // capture 2026-07-28: CONSTANT 1.5*1.0075, direction exact, zero spread (9 identical axis casts, wire
        // 12090 = 1.51125; the 07-06 gaussian read was sample noise)
        ProjectileTypeConfig bobber = ProjectileTypeConfig.builder(base.typeConfig(FishingBobber.KEY))
                .speed(1.51125)
                .spread(0.0)
                .syncInterval(0).velocitySyncInterval(0)
                .behavior(ctx -> new PseudoHook())
                .hookHalt(true) // the glued flash needs the same-tick halt + pin on the silent wire
                .selfHit(ProjectileTypeConfig.HitResponse.HIT) // MineMen: you CAN hook yourself (vanilla can't)
                .knockback(Knockback.rod())
                // SHOOTER-relative like vanilla (1.8 EntityLiving.damageEntity reads the indirect source = the angler)
                .knockbackSource(ProjectileTypeConfig.KnockbackSource.SHOOTER)
                .rodPull(new ProjectileTypeConfig.RodPull(0.1, 0.08, false, false))
                .build();
        ProjectileTypeConfig snowball = thrown(ProjectileTypeConfig.builder(Snowball.KEY));
        ProjectileTypeConfig egg = thrown(ProjectileTypeConfig.builder(Egg.KEY));
        ProjectileTypeConfig pearl = thrown(ProjectileTypeConfig.builder(base.typeConfig(Pearl.KEY))
                .behavior(PEARL_TELEPORT));
        // capture 2026-07-28 (78 arrows): deterministic - the vanilla spread gaussian pinned to +1 (dir += 0.0075
        // per axis pre-scale; 3 consecutive same-aim shots byte-identical, straight-up = (+179, 24179, +179) wire)
        ProjectileTypeConfig arrow = ProjectileTypeConfig.builder(base.typeConfig(Arrow.KEY))
                .spread(0.0).spreadBias(0.0075)
                .knockback(Knockback.arrow()).build();
        return ProjectileConfig.builder(base)
                // the 0.05 wire motY floor is universal on minemen projectiles, so make it the generic default every type inherits
                .defaults(ProjectileTypeConfig.builder(base.defaults()).wireMotYFloor(WIRE_MOTY_FLOOR).build())
                .typeConfigs(fireball, splash, bobber, snowball, egg, pearl, arrow)
                .shootables(new PseudoHook.Installer())
                .useItemAimSync(true) // MineMen launches on the CLICK-time aim (in-game: flick-throws never desync)
                .build();
    }

    // capture 2026-07-06: vanilla launch/flight, zero spread (the wire motY floor is the config-wide default above)
    private static ProjectileTypeConfig thrown(ProjectileTypeConfig.Builder builder) {
        return builder.spread(0.0).knockback(Knockback.projectile()).build();
    }
}
