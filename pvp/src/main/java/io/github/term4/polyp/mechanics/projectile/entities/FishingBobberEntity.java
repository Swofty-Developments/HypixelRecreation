package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ProjectileContext;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig.RodDurability;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig.RodPull;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityPositionSyncPacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fishing bobber: flies until it hooks an entity (0-damage hit, then pinned at 0.8 body height - what draws the 1.8
 * client's line to the victim) or lands; discards when the angler dies, drops the rod, or the line passes
 * {@code lineSnapDistance}. {@link #setHookedEntity} is the hook seam for custom rod behaviors (mmc18 pseudo-hook).
 *
 * <p>1.8 ground contact is a live damp-settle loop, never a frozen state (vanilla never records the hit block, so
 * {@code inGround} clears every tick with a {@code rand*0.2} damp); the predicting client settles the same way.
 * 26.1 keeps the frozen stick + 1200-tick despawn.
 *
 * <p>Both vanilla integrators apply gravity before drag (unlike throwables); the stamped {@code physicsOrder} picks
 * the placement - {@code DRAG_AFTER_MOVE} = 1.8 (move, gravity, drag) via the acceleration channel,
 * {@code DRAG_BEFORE_MOVE} = 26.1 (gravity, move, drag) via a pre-move subtract - and with it the water model.
 */
public class FishingBobberEntity extends ManagedProjectile {

    /** The angler's live bobber (set by the {@code FishingRod} launcher, cleared on remove). */
    public static final Tag<FishingBobberEntity> ACTIVE_BOBBER = Tag.Transient("polyp:active-bobber");

    /** Vanilla bobber box height, for the 1.8 water-coverage slabs (the physics box is a point). */
    private static final double BOX_HEIGHT = 0.25;
    private static final int GROUND_DESPAWN_TICKS = 1200;
    /** Vanilla hook box (both versions): clips the move; contact detection stays the point ray. */
    private static final BoundingBox MOVE_BOX = new BoundingBox(0.25, 0.25, 0.25);

    private final RodPull rodPull;
    private final RodDurability rodDurability;
    private final double lineSnapDistanceSq;
    private final boolean hookedMetadata;
    private final @Nullable Boolean hookHalt;
    private final boolean hookWater;
    private @Nullable Pos waterSyncedAt;
    private boolean waterDense;
    private final boolean hookStick;

    private @Nullable Entity hooked;
    /** 26.1 BOBBING: latched on water entry, never leaves (out-of-water bobbing just regains gravity). */
    private boolean bobbing;
    private long groundTicks;
    /** 1.8 ground contact last tick: apply the vanilla {@code rand*0.2} damp before this tick's physics. */
    private boolean contactPending;
    /** Grounded since the last real fall; gates the hit event/behavior to the first contact of a landing. */
    private boolean grounded;
    /** This tick's 1.8 water coverage was &gt; 0 - the collision drag ({@link #onBlockClip}) composes with it. */
    private boolean wet;

    /** Gravity (b/t) moved off the {@link Aerodynamics} so the bobber controls its placement; see class doc. */
    private double gravityBt;
    /** The stamped order asked for the 26.1 gravity-before-move placement. */
    private boolean modernOrder;
    private boolean latched;

    public FishingBobberEntity(@Nullable Entity shooter, @NotNull EntityType entityType,
                               ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        super(shooter, entityType, snap, effectiveConfig);
        ((FishingHookMeta) getEntityMeta()).setOwnerEntity(shooter);
        ProjectileContext ctx = ProjectileContext.of(snap, services());
        this.rodPull = FieldValue.resolve(effectiveConfig.rodPull, ctx, RodPull.VANILLA_1_8);
        this.rodDurability = FieldValue.resolve(effectiveConfig.rodDurability, ctx, RodDurability.VANILLA_1_8);
        double lineSnap = FieldValue.resolve(effectiveConfig.lineSnapDistance, ctx, 32.0);
        this.lineSnapDistanceSq = lineSnap * lineSnap;
        this.hookedMetadata = FieldValue.resolve(effectiveConfig.hookedMetadata, ctx, Boolean.TRUE);
        this.hookHalt = effectiveConfig.hookHalt != null ? effectiveConfig.hookHalt.resolve(ctx) : null;
        this.hookWater = FieldValue.resolve(effectiveConfig.hookWater, ctx, Boolean.TRUE);

        this.hookStick = FieldValue.resolve(effectiveConfig.hookStick, ctx, Boolean.FALSE);
    }

    /** Unset = the physics order's truth: 26.1 halts (deltaMovement ZERO on HOOKED_IN_ENTITY), 1.8 flies through. */
    private boolean haltOnHook() { return hookHalt != null ? hookHalt : modernOrder; }

    public @Nullable Entity getHookedEntity() { return hooked; }

    /** Hooks/releases the line; {@code hookHalt} zeroes the motion (1.8 keeps it - stale on release).
     *  Public so a custom behavior can flash it (mmc18 pseudo-hook). */
    public void setHookedEntity(@Nullable Entity entity) {
        this.hooked = entity;
        if (hookedMetadata) ((FishingHookMeta) getEntityMeta()).setHookedEntity(entity);
        if (entity != null && haltOnHook()) setVelocity(Vec.ZERO);
    }

    @Override
    protected void onImpact(@Nullable Entity hitEntity) {
        super.onImpact(hitEntity);
        if (hitEntity == null) return;
        setHookedEntity(hitEntity);
        // 1.8 completes the hook tick's move THROUGH the victim (the pin starts next tick, carried by the normal
        // sync); halting + same-tick pin is the 26.1 state machine / the mmc18 silent-wire glued flash
        if (haltOnHook()) setDeflected();
    }

    @Override
    protected void movementTick() {
        if (hooked != null) {
            if (hookedValid()) return; // no physics while hooked; the pin lands in updateProjectile
            setHookedEntity(null);
        }
        if (!latched) latch();
        if (modernOrder) {
            if (tickModernWater()) return; // the FLYING -> BOBBING damp tick doesn't move
        } else {
            if (contactPending) {
                // vanilla's next-tick unstick (xTile is never set, so inGround always clears): damp, then physics
                contactPending = false;
                ThreadLocalRandom r = ThreadLocalRandom.current();
                velocityBt = new Vec(velocityBt.x() * r.nextFloat() * 0.2f,
                        velocityBt.y() * r.nextFloat() * 0.2f,
                        velocityBt.z() * r.nextFloat() * 0.2f);
                this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
            }
            tickLegacyWater();
        }
        super.movementTick();
        // a real fall (settle nudges stay under 0.1) re-arms the landing events for the next ground contact
        if (grounded && !contactPending && Math.abs(velocityBt.y()) > 0.1) grounded = false;
    }

    private void latch() {
        Aerodynamics aero = getAerodynamics();
        gravityBt = aero.gravity();
        modernOrder = physicsOrder == ProjectileTypeConfig.PhysicsOrder.DRAG_BEFORE_MOVE;
        physicsOrder = ProjectileTypeConfig.PhysicsOrder.DRAG_AFTER_MOVE; // both integrators drag after the move
        setAerodynamics(new Aerodynamics(0, aero.horizontalAirResistance(), aero.verticalAirResistance()));
        latched = true;
    }

    /** 1.8: {@code motY += 0.04 * (2*coverage - 1)} (buoyant when submerged) rides the acceleration channel so it
     *  lands before drag; in water drag tightens to {@code 0.92*0.9} with an extra {@code 0.8} vertical. */
    private void tickLegacyWater() {
        double coverage = hookWater ? waterCoverage() : 0;
        wet = coverage > 0;
        float f2 = 0.92f;
        double verticalExtra = 1.0;
        if (wet) {
            f2 = (float) (f2 * 0.9);
            verticalExtra = 0.8;
        }
        setAcceleration(new Vec(0, gravityBt * (2 * coverage - 1), 0));
        setAerodynamics(new Aerodynamics(0, f2, verticalExtra * f2));
    }

    /** 26.1 water states + the gravity-before-move placement. Returns {@code true} on the water-entry tick (26.1
     *  damps the velocity and returns without moving). */
    private boolean tickModernWater() {
        Pos pos = getPosition();
        int blockY = (int) Math.floor(pos.y());
        double liquidHeight = hookWater ? Math.max(0, waterSurface(pos.x(), pos.y(), pos.z()) - blockY) : 0;
        boolean inWater = liquidHeight > 0;
        if (!bobbing && inWater) {
            velocityBt = velocityBt.mul(0.3, 0.2, 0.3);
            this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
            bobbing = true;
            return true;
        }
        if (bobbing && inWater) {
            double force = pos.y() + velocityBt.y() - (blockY + liquidHeight);
            if (Math.abs(force) < 0.01) force += Math.signum(force) * 0.1;
            velocityBt = new Vec(velocityBt.x() * (subStepping() ? 0.9 : TickScaler.dragPerTick(scopeSubject(), 0.9)),
                    velocityBt.y() - (subStepping() ? force * ThreadLocalRandom.current().nextFloat() * 0.2
                            : TickScaler.gravityPerTick(scopeSubject(), force * ThreadLocalRandom.current().nextFloat() * 0.2)),
                    velocityBt.z() * (subStepping() ? 0.9 : TickScaler.dragPerTick(scopeSubject(), 0.9)));
        }
        // 26.1 gates gravity on the ground too - a rested hook stays put (contact zeroed it, see onBlockClip)
        if (!inWater && !isOnGround()) velocityBt = velocityBt.sub(0,
                subStepping() ? gravityBt : TickScaler.gravityPerTick(scopeSubject(), gravityBt), 0);
        this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
        return false;
    }

    private boolean touchesWater(Point p) {
        if (getInstance() == null) return false;
        MechanicsWorld world = MechanicsWorld.of(this);
        int x = (int) Math.floor(p.x()), z = (int) Math.floor(p.z());
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return false;
        return world.getBlock(x, (int) Math.floor(p.y()), z).compare(Block.WATER);
    }

    /** 1.8 coverage: the 0.25 bobber box in 5 slabs, each counting when the water surface is above its bottom. */
    private double waterCoverage() {
        Pos pos = getPosition();
        double coverage = 0;
        int lastBy = Integer.MIN_VALUE;
        double surface = Double.NEGATIVE_INFINITY;
        for (int slab = 0; slab < 5; slab++) {
            double slabMin = pos.y() + BOX_HEIGHT * slab / 5;
            int by = (int) Math.floor(slabMin);
            if (by != lastBy) { // the surface is per-block: the 0.2-tall span hits at most two
                lastBy = by;
                surface = waterSurface(pos.x(), slabMin, pos.z());
            }
            if (surface > slabMin) coverage += 1.0 / 5;
        }
        return coverage;
    }

    /** Water surface Y of the block containing the point, or {@code -inf}: full when water continues above, else the
     *  1.8 level height ({@code 1 - (level+1)/9}, source = 8/9; falling counts as source). */
    private double waterSurface(double x, double y, double z) {
        Instance instance = getInstance();
        if (instance == null) return Double.NEGATIVE_INFINITY;
        MechanicsWorld world = MechanicsWorld.of(this);
        int bx = (int) Math.floor(x), by = (int) Math.floor(y), bz = (int) Math.floor(z);
        Block block = world.getBlock(bx, by, bz);
        if (!block.compare(Block.WATER)) return Double.NEGATIVE_INFINITY;
        if (world.getBlock(bx, by + 1, bz).compare(Block.WATER)) return by + 1;
        String level = block.getProperty("level");
        int lvl = level != null ? Integer.parseInt(level) : 0;
        if (lvl >= 8) lvl = 0;
        return by + 1 - (lvl + 1) / 9.0;
    }

    @Override
    protected boolean canHit(@NotNull Entity entity) {
        return !bobbing && super.canHit(entity); // 26.1 only collides while FLYING; 1.8 never enters the state
    }

    @Override
    protected BoundingBox moveBox() { return MOVE_BOX; }

    // hookStick freezes into the block (arrow-like). Else: 26.1 has no stuck state (onBlockClip); 1.8 sticks on the
    // contact RAY (hold + damp cycle).
    @Override
    protected boolean stickOnBlockContact() { return hookStick || !modernOrder; }

    @Override
    protected void onBlockClip(PhysicsResult physics) {
        if (!physics.isOnGround() && !physics.collisionX() && !physics.collisionZ()) return; // ceiling-only: full drag
        if (modernOrder) {
            // 26.1: a FLYING contact (ground or wall) stops the hook dead; it falls from rest next tick
            if (!bobbing) setVelocityBt(Vec.ZERO);
        } else {
            // 1.8 f2 = 0.5 on onGround/positionChanged move ticks (the box clipped without the ray firing)
            float f2 = wet ? 0.5f * 0.9f : 0.5f;
            setAerodynamics(new Aerodynamics(0, f2, (wet ? 0.8 : 1.0) * f2));
        }
    }

    @Override
    protected Pos stickPlacement(Pos resolvedPosition, Vec flightDir) {
        // stick: pull back to the face like an arrow. Slide (vanilla): the ray-hit tick skips the move - hold at the
        // pre-move position (the predicting client halts there too; landing at the face snaps it forward on the resync)
        return hookStick ? super.stickPlacement(resolvedPosition, flightDir) : getPosition();
    }

    @Override
    protected boolean freezeOnStick() { return hookStick; } // vanilla never freezes (contact halts, damp-settles); hookStick embeds it

    @Override
    protected boolean onStuck() {
        contactPending = true;
        // vanilla zeroes ticksInAir on every unstick, so the owner (au >= 5 gate) can't hook themselves walking
        // over their grounded bobber
        rearmShooterImmunity();
        if (grounded) return false; // settle-cycle re-contacts: damp only, no repeated hit events
        grounded = true;
        return super.onStuck();
    }

    private boolean hookedValid() {
        return hooked != null && !hooked.isRemoved() && hooked.getInstance() == getInstance()
                && !(hooked instanceof LivingEntity living && living.isDead())
                && WorldPolicy.canAffect(this, hooked); // a target that left the bobber's world slips the line
    }

    /** Holds the bobber at {@code target}; on a silent wire a changed pin is pushed explicitly - the 1.8 client has
     *  no hooked metadata, its "line to the victim" is the bobber BEING at the victim. */
    private void pinTo(Pos target) {
        Pos pos = getPosition();
        Pos pinned = target.withView(pos.yaw(), pos.pitch());
        refreshPosition(pinned, false, false);
        if (getSynchronizationTicks() <= 0 && !pinned.samePoint(lastSyncedPosition)) {
            sendPacketToViewersAndSelf(new EntityTeleportPacket(getEntityId(), pinned, Vec.ZERO, 0, false));
            this.lastSyncedPosition = pinned;
        }
    }

    @Override
    protected void updateProjectile(long time) {
        super.updateProjectile(time);
        if (isRemoved()) return;
        if (shouldStopFishing()) { remove(); return; }
        // 26.1 despawns a resting (non-stuck) bobber after 1200 grounded ticks (life); 1.8's counter is dead code.
        // A hookStick bobber is frozen-stuck instead - the stuck lifecycle owns its removal (unstick on block break).
        if (modernOrder && !isStuck() && isOnGround()) {
            if (++groundTicks >= TickScaler.duration(scopeSubject(), GROUND_DESPAWN_TICKS, ProjectileSystem.KEY)) { remove(); return; }
        } else {
            groundTicks = 0;
        }
        // pin at end-of-tick, past the hit tick's halt - the hook packets reach a predicting client that's already
        // ahead by its latency, so the pin must ride the SAME tick as the hook or it reads as a fly-through
        if (hooked != null && hookedValid()) {
            pinTo(hooked.getPosition().add(0, hooked.getBoundingBox().height() * 0.8, 0));
        }
        // COMPAT display fix, not preset wire: a silent wire only breaks for viewers whose generation cannot
        // predict THIS hook's water physics (1.8 EntityFishHook buoys client-side, MCP-919 l.476; 26.1 latches
        // BOBBING at first contact); hookWater off = neither predicts. Those viewers ride a dense escort
        // through the water window, the air stays silent
        if (hooked == null && velocitySyncInterval <= 0 && getSynchronizationTicks() <= 0) {
            Pos pos = getPosition();
            if (!waterDense) {
                waterDense = touchesWater(pos) || touchesWater(pos.add(velocityBt.mul(2)));
            }
            if (waterDense && (waterSyncedAt == null || pos.distanceSquared(waterSyncedAt) > 1.0e-8)) {
                waterSyncedAt = pos;
                var clientInfo = io.github.term4.polyp.Polyp.getInstance().clientInfo();
                var sync = new EntityPositionSyncPacket(getEntityId(), pos, Vec.ZERO, pos.yaw(), pos.pitch(), isOnGround());
                for (Player viewer : getViewers()) {
                    if (hookWater && clientInfo != null && clientInfo.isLegacy(viewer) == !modernOrder) {
                        continue; // their generation predicts this physics
                    }
                    if (viewer instanceof io.github.term4.polyp.platform.player.OptimizedPlayer op
                            && !op.compat().hookPredictionEscort()) continue;
                    viewer.sendPacket(sync);
                    viewer.sendPacket(new EntityVelocityPacket(getEntityId(), wireVelocityFor(viewer))); // per-viewer: 1.8 gets the un-dilated step
                }
            }
        }
    }

    private boolean shouldStopFishing() {
        Entity owner = getShooter();
        if (owner == null || owner.isRemoved()) return true;
        if (!WorldPolicy.canAffect(this, owner)) return true; // the angler left this world: the line dies with it (vanilla cross-dimension parity)
        if (owner instanceof LivingEntity living) {
            if (living.isDead()) return true;
            if (!holdsRod(living)) return true;
        }
        return getPosition().distanceSquared(owner.getPosition()) > lineSnapDistanceSq;
    }

    private static boolean holdsRod(LivingEntity holder) {
        return holder.getItemInMainHand().material() == Material.FISHING_ROD
                || holder.getItemInOffHand().material() == Material.FISHING_ROD;
    }

    /** Reels the line in and removes the bobber; returns the {@code rodDurability} cost (hooked entity / ground / 0). */
    public int retrieve() {
        int cost = 0;
        if (hookedValid()) {
            pullHooked();
            cost = rodDurability.entity();
        }
        if (isStuck() || isOnGround()) cost = rodDurability.ground();
        remove();
        return cost;
    }

    private void pullHooked() {
        Entity owner = getShooter();
        if (owner == null || hooked == null) return;
        if (hooked instanceof Player && !rodPull.pullPlayers()) return;
        Pos anglerPos = owner.getPosition();
        Pos pos = getPosition();
        Vec toAngler = new Vec(anglerPos.x() - pos.x(), anglerPos.y() - pos.y(), anglerPos.z() - pos.z());
        Vec delta = toAngler.mul(rodPull.factor())
                .add(0, Math.sqrt(toAngler.length()) * rodPull.yBoost(), 0);
        if (hooked instanceof Player player) {
            Vec folded = MotionTracker.positionDelta(player).add(delta); // b/t: current motion + pull
            if (!rodPull.wireVelocity()) {
                MotionTracker.foldDelivered(player, folded); // 1.8: server-tracked only, surfaces in the next hit's KB
                return;
            }
            Vec out = folded.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
            Services s = services();
            KnockbackSystem kb = s != null ? s.knockback() : null;
            if (kb != null) kb.deliver(player, out); // the knockback wire owns quantize + the legacy-exact bridge (b/s)
            else player.setVelocity(out);
        } else {
            hooked.setVelocity(hooked.getVelocity().add(delta.mul(ServerFlag.SERVER_TICKS_PER_SECOND)));
        }
    }

    @Override
    public void remove() {
        Entity owner = getShooter();
        if (owner != null && owner.getTag(ACTIVE_BOBBER) == this) owner.removeTag(ACTIVE_BOBBER);
        super.remove();
    }
}
