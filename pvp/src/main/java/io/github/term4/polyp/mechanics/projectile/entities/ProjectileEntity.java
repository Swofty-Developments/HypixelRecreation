package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.world.ExternallyTickable;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.util.Directions;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.tracking.motion.FluidFlow;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityTeleportPacket;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Base projectile entity: 1.8-style physics, block stick, entity hits ({@code docs/projectiles-design.md}).
 * A stuck arrow freezes but {@link #resyncStuck()} periodically re-asserts pos + rotation so a 1.8 client (via Via)
 * self-heals; modern clients hold via {@code inGround} metadata. Velocity is b/t internally ({@code super.velocity}
 * mirrors b/s); {@link Aerodynamics} constants are read live.
 */
public abstract class ProjectileEntity extends Entity implements ExternallyTickable {

    // @ApiStatus.Internal override: super is exactly this field write + dispatcher().updateElement (verified
    // 26.2, re-verify on bumps) - an externally ticked entity in the global dispatcher double-ticks
    @Override protected void refreshCurrentChunk(@NotNull net.minestom.server.instance.Chunk chunk) {
        if (MechanicsWorld.externallyTicked(this)) {
            currentChunk = chunk;
            return;
        }
        super.refreshCurrentChunk(chunk);
    }

    public static final int DEFAULT_SHOOTER_IMMUNITY_TICKS = 5;

    /** Grows the zero-size projectile box {@code 0.3} each side (vanilla grows the target instead). */
    public static final double DEFAULT_ENTITY_HIT_GROW = 0.3;

    /** Marks a non-living, non-projectile entity as a projectile-collision target (vanilla {@code ad()}, which Minestom lacks): primed TNT, boats. A {@link ProjectileEntity} opts in via {@link #collidableTarget()}. */
    public static final Tag<Boolean> PROJECTILE_COLLIDABLE = Tag.Boolean("polyp:projectile-collidable");

    /** Vanilla {@code inflate(1.0)} on the {@link #leftOwnerImmunity} check. */
    private static final double LEFT_OWNER_INFLATE = 1.0;

    /** Render rotation eases this fraction toward the motion direction each tick (1.8 {@code EntityArrow}, 26.1 {@code Projectile#lerpRotation}), so a stuck arrow keeps the lagged angle, not the raw velocity. */
    private static final float ROTATION_LERP = 0.2f;

    protected int shooterImmunityTicks = DEFAULT_SHOOTER_IMMUNITY_TICKS;
    /** Alive-tick the shooter is immune until after a deflect; {@code <= 0} = none. */
    private long shooterImmuneUntilAlive;
    protected double entityHitGrow = DEFAULT_ENTITY_HIT_GROW;
    /** {@code <= 0} = never (vanilla arrow, the edge-slide fix). Gates {@link #sendPacketToViewers}. */
    protected int velocitySyncInterval;
    /** {@code false} = silent between position syncs, clients predict from the spawn velocity. */
    protected boolean broadcastMovement;
    /** Seeded at 1 - vanilla's first correction comes a full interval after spawn. */
    private long autoVelocityCounter = 1;
    /** 1.8 = {@code DRAG_AFTER_MOVE} (default), 26.1 = {@code DRAG_BEFORE_MOVE}. */
    protected ProjectileTypeConfig.PhysicsOrder physicsOrder = ProjectileTypeConfig.PhysicsOrder.DRAG_AFTER_MOVE;
    /** 26.1 model: the shooter is immune until the projectile leaves its box. */
    protected boolean leftOwnerImmunity;
    private boolean leftOwner;
    /** Blocks along the flight dir the stuck projectile is pulled back so the tip pokes out of the block face (vanilla 0.05). */
    protected double stickPullback = 0.05;
    /** Wire-only |motY| floor on every broadcast velocity ({@code 0} = off); the sim is untouched. MineMen throwables 0.05. */
    private double wireMotYFloor;
    /** Blocks/tick ({@code super.velocity} mirrors b/s). */
    protected Vec velocityBt = Vec.ZERO;
    private double waterDrag = 1.0;
    private double waterPush;
    private ProjectileTypeConfig.WaterModel waterModel = ProjectileTypeConfig.WaterModel.LEGACY;
    private boolean legacyBlockRay = false;
    private boolean inWater;
    protected final boolean inWater() { return inWater; }
    /** Added before drag (vanilla fireball {@code mot += dir; mot *= drag}); ZERO for ballistic projectiles. */
    protected Vec acceleration = Vec.ZERO;
    /** Knockback origin: vanilla uses the shooter, not the projectile. */
    protected Pos shooterOriginPos;
    protected @Nullable Entity shooter;
    /** Launch position (wire-grid-quantized under lockstep); the first-tick spawn packet carries it. */
    private @Nullable Pos spawnPosition;

    // stuck state: collisionDirection != null means stuck
    /** Single-axis face normal of the hit surface (signed travel direction on the hit axis). */
    protected @Nullable Vec collisionDirection;
    /** The clipped contact position on the hit tick (centre just short of the face/entity); getPosition() at impact
     *  time is still the PRE-MOVE position (vanilla pearl semantics), so target-reshaping behaviors floor THIS. */
    private @Nullable Pos impactPosition;
    /** The face struck by the current BLOCK impact (TOP = landed on a floor); null for entity hits. Unlike
     *  {@link #collisionDirection} it is latched for break-on-hit projectiles too. */
    private @Nullable BlockFace impactFace;
    private @Nullable Point impactCell;
    private @Nullable Shape impactShape;
    private final ArrayDeque<Pos> stepHistory = new ArrayDeque<>();
    protected @Nullable Point stuckCollisionPoint;
    private @Nullable Pos stuckPlacement;
    private long stuckSyncCounter;
    /** Throttles the in-flight teleport to {@code syncInterval}. Seeded at 0, not 1: 1.8's tracker fires on
     *  {@code updateCounter % updateFrequency == 0}, true at counter 0, so the first correction lands the tick
     *  after spawn - a projectile that dies inside one interval otherwise reaches the client with no position. */
    private long flightSyncCounter;

    private @Nullable PhysicsResult previousPhysicsResult;
    private float prevYaw, prevPitch;
    private boolean rotationInitialized;
    private float stuckYaw, stuckPitch;
    private boolean justBecameStuck;
    /** A hit bounced the projectile this tick (velocity reversed, not removed); the forward move stops at the hit point. */
    private boolean deflectedThisTick;
    /** {@code deflectParticles} opt-in: on a deflect/pass-through, let a subclass spawn a server-side crit trail. */
    protected boolean deflectVisible;
    /** Removal requested inside movementTick; applied in {@link #tick} after super.tick so {@code instance} isn't nulled mid-tick (NPE). */
    private boolean pendingRemove;
    /** Captured off the launching item at launch (0 = none). */
    private int powerLevel;
    private int punchLevel;
    private int flameLevel;

    protected ProjectileEntity(@Nullable Entity shooter, @NotNull EntityType entityType) {
        super(entityType);
        this.shooter = shooter;
        this.shooterOriginPos = shooter != null ? shooter.getPosition() : Pos.ZERO;
        this.preventBlockPlacement = false;
        if (getEntityMeta() instanceof ProjectileMeta meta) meta.setShooter(shooter);
        // zero-size box: collision points resolve exactly on block boundaries (any size overshoots the inGround check)
        setBoundingBox(0, 0, 0);
    }

    /** Called when the projectile hits an entity. Return {@code true} to remove the projectile. */
    protected boolean onHit(@NotNull Entity entity) { return false; }

    /** Called when the projectile sticks in a block. Return {@code true} to remove the projectile. */
    protected boolean onStuck() { return false; }

    /** Called when the projectile unsticks (block broken). */
    protected void onUnstuck() {}

    /** Per-tick subclass update while alive (pickup, despawn timers); also runs while stuck. */
    protected void updateProjectile(long time) {}

    /** Whether this projectile can hit {@code entity} (shooter immunity is handled separately). */
    protected boolean canHit(@NotNull Entity entity) {
        if (!WorldPolicy.canAffect(this, entity)) return false; // cross-world: a main-map projectile passes through game players
        // vanilla ad(): a live non-spectator target, or a solid entity opting in via the tag (primed TNT).
        // A collidable projectile (fireball) is a strike target only for a NON-collidable one - two fireballs pass through each other (Hypixel).
        if (entity instanceof ProjectileEntity pe) return pe.collidableTarget() && !collidableTarget();
        if (entity instanceof LivingEntity living)
            return !living.isDead() && (!(entity instanceof Player p) || p.getGameMode() != GameMode.SPECTATOR);
        return Boolean.TRUE.equals(entity.getTag(PROJECTILE_COLLIDABLE));
    }

    /** Whether other projectiles/attacks can hit this one (vanilla {@code ad()}/canBeCollidedWith). Default {@code false}; a fireball overrides it (collidable + deflectable). */
    protected boolean collidableTarget() { return false; }

    // vanilla ad(): only a collidable projectile (a fireball) is a strike target for others' collision sweeps
    @Override
    public boolean hasEntityCollision() { return collidableTarget(); }

    /** The box for THIS projectile's OWN collision (block + entity sweep); defaults to the entity box. A fireball overrides it to a
     *  POINT so it detonates raytrace-like, while {@link #getBoundingBox()} (its real 1x1 box) is what OTHERS hit to deflect it (vanilla decouples the two). */
    protected BoundingBox collisionBox() { return getBoundingBox(); }

    /** Deflects this projectile off {@code deflector} (redirect along its look + reassign ownership); default no-op. Returns whether it deflected. */
    public boolean deflectBy(@Nullable Entity deflector) { return false; }

    /** Reassigns the owner + re-arms leave-owner immunity. The wire spawn needs a live owner id: clients DISCARD
     *  ownerless bobbers and 1.8 drops ownerless spawn velocity. */
    public void reassignShooter(@Nullable Entity newShooter) {
        this.shooter = newShooter;
        if (getEntityMeta() instanceof ProjectileMeta meta) meta.setShooter(newShooter);
        this.leftOwner = false;
        // SHOOTER-relative KB must push away from the CURRENT owner, not the original thrower's launch spot
        if (newShooter != null) this.shooterOriginPos = newShooter.getPosition();
    }

    public @Nullable Entity getShooter() { return shooter; }

    /** Tick-scaling subject: the SHOOTER while it exists. A projectile resolves no player scope of its own, and
     *  pre-spawn it resolves none at all - what you throw inherits your time frame. */
    protected @NotNull Entity scopeSubject() {
        return shooter != null && !shooter.isRemoved() ? shooter : this;
    }

    /** Vanilla model: N native steps per server tick instead of one stretched step. The two must NEVER mix -
     *  sub-stepping takes its speed from the step COUNT, so a scaled velocity on top multiplies it again. */
    protected boolean subStepping() {
        return TickScaler.stepsPerTick(scopeSubject()) > 1.0;
    }

    public @NotNull Pos getShooterOriginPos() { return shooterOriginPos; }

    public void setShooterOriginPos(@NotNull Pos pos) { this.shooterOriginPos = pos; }

    public boolean isStuck() { return collisionDirection != null; }

    /** The clipped contact position of the current hit (block or entity), null before any contact. */
    public @Nullable Pos impactPosition() { return impactPosition; }

    /** The face struck by the current block impact ({@code TOP} = landed on a floor), null for entity hits. */
    public @Nullable BlockFace impactFace() { return impactFace; }

    /** Cell of the struck block; face planes derive from cell + {@link #impactShape()}, not from the
     *  clipped {@link #impactPosition()}. */
    public @Nullable Point impactCell() { return impactCell; }

    /** Collision shape of the struck block, null before any block contact. */
    public @Nullable Shape impactShape() { return impactShape; }

    /** Prior tick-start positions, most recent first (the current one is {@code getPosition()} at impact
     *  time); bounded, oldest = the spawn. */
    public Iterable<Pos> stepHistory() { return stepHistory; }

    /** The wire-grid spawn position, or {@code null} before launch. */
    public @Nullable Pos getSpawnPosition() { return spawnPosition; }
    public void setSpawnPosition(@NotNull Pos pos) { this.spawnPosition = pos; }

    /** The exact point on the block face this projectile stuck to (arrows), or {@code null} if not stuck. */
    public @Nullable Point getStuckPoint() { return stuckCollisionPoint; }

    /** Position of the block this projectile is embedded in (just past the stuck face), or {@code null} if not stuck. */
    public @Nullable Point getStuckBlockPosition() {
        if (stuckCollisionPoint == null || collisionDirection == null) return null;
        return stuckCollisionPoint.add(collisionDirection.mul(0.5)).asBlockVec();
    }

    /** The block this projectile is stuck in (queried live), or {@code null} if not stuck / no instance. */
    public @Nullable Block getStuckBlock() {
        Point pos = getStuckBlockPosition();
        return pos != null && instance != null ? MechanicsWorld.of(this).getBlock(pos) : null;
    }

    public void setShooterImmunityTicks(int ticks) { this.shooterImmunityTicks = ticks; }

    /** {@code false} = block collisions only: no entity hits, deflects or self-hits (cosmetic/replayed projectiles). */
    public void setEntityHits(boolean v) { this.entityHits = v; }
    private boolean entityHits = true;

    public void setEntityHitGrow(double grow) { this.entityHitGrow = grow; }

    public void setVelocitySyncInterval(int interval) { this.velocitySyncInterval = interval; }

    /** Water behavior: {@code drag} replaces both air drags while in water ({@code 1} = unchanged), {@code push} = the per-tick current impulse. */
    public void setWaterPhysics(double drag, double push, ProjectileTypeConfig.WaterModel model) {
        this.waterDrag = drag;
        this.waterPush = push;
        this.waterModel = model;
    }

    public void setBroadcastMovement(boolean v) { this.broadcastMovement = v; }

    public void setLegacyBlockRay(boolean v) { this.legacyBlockRay = v; }

    /** Box swept against BLOCKS instead of the collision box; entity hits keep the collision box + grow. */

    public void setPhysicsOrder(@NotNull ProjectileTypeConfig.PhysicsOrder order) { this.physicsOrder = order; }

    public void setLeftOwnerImmunity(boolean v) { this.leftOwnerImmunity = v; }

    public void setStickPullback(double v) { this.stickPullback = v; }

    public void setWireMotYFloor(double v) { this.wireMotYFloor = v; }

    /** Per-tick acceleration (b/t) folded in before drag - the fireball's self-propulsion. */
    public void setAcceleration(@NotNull Vec a) { this.acceleration = a; }

    public int powerLevel() { return powerLevel; }
    public int punchLevel() { return punchLevel; }
    public int flameLevel() { return flameLevel; }
    public void setProjectileEnchants(int power, int punch, int flame) { this.powerLevel = power; this.punchLevel = punch; this.flameLevel = flame; }

    /** Re-arms shooter immunity for another {@link #shooterImmunityTicks} (vanilla {@code as = 0} after a deflect, so
     *  a bounced-back arrow can't instantly re-hit the shooter / loop on a self-deflect). */
    protected void rearmShooterImmunity() { this.shooterImmuneUntilAlive = getAliveTicks() + shooterImmunityTicks; }

    protected void setDeflected() { this.deflectedThisTick = true; }

    /** Velocity in blocks/tick. */
    public @NotNull Vec velocityBt() { return velocityBt; }

    /**
     * Sets velocity in client b/t (TPS-rescaled to the server tick rate so the arc is TPS-invariant; identity at 20),
     * mirrored to {@code super.velocity}. Silent - the client predicts from spawn velocity; use {@link #setVelocity} to broadcast a redirect.
     */
    public void setVelocityBt(@NotNull Vec bt) {
        this.velocityBt = subStepping() ? bt : TickScaler.fromClientVelocity(scopeSubject(), bt);
        this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    /** Explicit velocity set (redirect / homing): broadcasts even for arrows, so unlike {@link #setVelocityBt} it reaches 1.8 clients through the per-tick suppression. */
    @Override
    public void setVelocity(@NotNull Vec velocity) {
        this.velocityBt = velocity.div(ServerFlag.SERVER_TICKS_PER_SECOND);
        this.velocity = velocity;
        // re-aiming a stuck projectile re-launches it (an explosion boost): clear the stuck state, the client's
        // inGround freeze included, and fall again (stick set no-gravity)
        if (isStuck() && !velocityBt.isZero()) {
            EventDispatcher.call(new ProjectileUncollideEvent(this));
            collisionDirection = null;
            stuckCollisionPoint = null;
            setNoGravity(false);
            onUnstuck();
        }
        if (!isStuck()) broadcastWireVelocity();
    }

    @Override
    public void tick(long time) {
        if (!MechanicsWorld.ownsCurrentTick(this)) return;
        if (isStuck()) {
            if (isRemoved()) return;
            updateProjectile(time);
            if (isRemoved()) return;
            if (stuckSyncCounter++ % Math.max(1L, getSynchronizationTicks()) == 0) resyncStuck();
            if (shouldUnstuck()) unstick();
            return;
        }
        super.tick(time);
        if (pendingRemove) {
            pendingRemove = false;
            breakParticles();
            remove();
            return;
        }
        if (!isRemoved()) updateProjectile(time);
        if (!isRemoved()) anchorLegacyViewersUnderDilation();
    }

    /** A 1.8 client runs NATIVE local physics on a dilated projectile (it can't be told the tick rate), so its arc
     *  diverges from the server's between the sparse syncs and snaps each one. Pin legacy viewers to the server
     *  position every tick (the replay's legSets doctrine); a no-op off dilation, where their physics already matches. */
    private void anchorLegacyViewersUnderDilation() {
        if (isStuck() || TickScaler.stepsPerTick(scopeSubject()) == 1.0) return;
        Pos pos = getPosition();
        Vec step = legacyVelocityForPacket();
        var clientInfo = Polyp.getInstance().clientInfo();
        for (Player viewer : getViewers()) {
            if (clientInfo.isLegacy(viewer)) viewer.sendPacket(new EntityTeleportPacket(getEntityId(), pos, step, 0, onGround));
        }
    }

    /** Carried remainder of {@link TickScaler#stepsPerTick} - a fractional rate (sim 30 on a 20 server) alternates 1 and 2. */
    private double stepCarry;

    // vanilla runs the whole tick more often for a faster /tick rate, never a stretched step; one stretched step
    // drifts ~0.5 blocks/s at sim 40
    @Override
    protected void movementTick() {
        double rate = TickScaler.stepsPerTick(scopeSubject());
        if (rate <= 1.0) { // server already ticks at or above the simulated rate
            movementStep(false);
            return;
        }
        stepCarry += rate;
        int steps = (int) stepCarry;
        stepCarry -= steps;
        for (int i = 0; i < steps; i++) {
            movementStep(true);
            // a step that ended the flight owns the tick - the rest would sim a dead projectile
            if (pendingRemove || isRemoved() || isStuck() || vehicle != null) break;
        }
    }

    /** One step. {@code nativeStep} = a real vanilla-rate tick (sub-stepping); else the coarse scaled step. */
    private void movementStep(boolean nativeStep) {
        this.gravityTickCount = isStuck() ? 0 : gravityTickCount + 1;
        if (vehicle != null || isStuck()) return;

        final MechanicsWorld world = MechanicsWorld.of(this);
        if (world.isInVoid(position)) {
            pendingRemove = true;
            return;
        }

        // water sensing runs BEFORE the motion step (both eras). The box is the VANILLA entity box - the physics
        // box is a point, which would invert the 1.8 Y-inset and never detect
        if (waterDrag < 1.0 || waterPush > 0) {
            if (waterModel == ProjectileTypeConfig.WaterModel.MODERN) {
                var sample = FluidFlow.itemModernSample(world, position, getEntityType().registry().boundingBox(), Block.WATER);
                inWater = sample.height() > 0;
                if (inWater && waterPush > 0) {
                    Vec cur = sample.current(nativeStep ? waterPush : TickScaler.gravityPerTick(scopeSubject(), waterPush), velocityBt.x(), velocityBt.z());
                    if (!cur.isZero()) {
                        velocityBt = velocityBt.add(cur);
                        this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
                    }
                }
            } else {
                Vec flow = FluidFlow.waterContact(world, position, getEntityType().registry().boundingBox());
                inWater = flow != null;
                if (inWater && waterPush > 0 && !flow.isZero()) {
                    velocityBt = velocityBt.add(flow.mul(nativeStep ? waterPush : TickScaler.gravityPerTick(scopeSubject(), waterPush)));
                    this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
                }
            }
        }

        // seed the render rotation at the launch direction on the first moving tick (vanilla onUpdate first-tick snap)
        if (!rotationInitialized && velocityBt.lengthSquared() > 1e-8) {
            prevYaw = Directions.yaw(velocityBt);
            prevPitch = Directions.pitch(velocityBt);
            rotationInitialized = true;
        }

        // 26.1 applies drag/gravity before the move (1.8 after); a coasting projectile skips both (constant velocity)
        if (physicsOrder == ProjectileTypeConfig.PhysicsOrder.DRAG_BEFORE_MOVE && !coasting()) applyDragGravity(nativeStep);
        PhysicsResult physics = legacyBlockRay
                ? LegacyBlockRay.sweep(world, collisionBox(), position, velocityBt, previousPhysicsResult)
                : world.sweepLoaded(collisionBox(), position, velocityBt, previousPhysicsResult, true);
        boolean blockContact = physics.hasCollision() && stickOnBlockContact();
        BoundingBox moveBox = moveBox();
        if (moveBox != collisionBox() && !blockContact) {
            // dual model (vanilla hook): the sweep above is the contact RAY, the real box clips the move
            physics = world.sweepLoaded(moveBox, position, velocityBt, physics, true);
        }
        this.previousPhysicsResult = physics;
        // Minestom caps even a non-colliding swept move at (1 - EPSILON)*v; a 1e-6-high detonation center shifts
        // the explosion KB by a wire unit, so restore the full move when nothing was hit
        Pos resolved = physics.hasCollision() ? physics.newPosition() : position.add(velocityBt);
        Pos newPosition = CollisionUtils.applyWorldBorder(world.worldBorder(), position, resolved);
        // shooter immunity: 26.1 = until the projectile leaves the shooter's box, 1.8 = fixed ticks
        if (entityHits) {
            if (leftOwnerImmunity && !leftOwner && shooter != null && !withinShooterBox(position)) leftOwner = true;
            boolean shooterImmune = (leftOwnerImmunity ? (shooter != null && !leftOwner)
                    : getAliveTicks() < shooterImmunityTicks) || getAliveTicks() < shooterImmuneUntilAlive;
            Collection<EntityCollisionResult> hits = world.sweepEntities(
                    collisionBox().growSymmetrically(entityHitGrow, entityHitGrow, entityHitGrow), position, velocityBt, 3,
                    e -> e != this && !(shooterImmune && e == shooter) && canHit(e), physics);
            if (!hits.isEmpty()) {
                EntityCollisionResult hit = hits.iterator().next();
                var event = new ProjectileCollideWithEntityEvent(this, hit.collisionPoint().asPos(), hit.entity());
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    this.impactPosition = hit.collisionPoint().asPos();
                    // the hitter is CONSUMED by a deflect even if its own hit would bounce (vanilla: the arrow
                    // die()s on a returned-true damageEntity)
                    boolean deflected = hit.entity() instanceof ProjectileEntity target && target.deflectBy(getShooter());
                    if (onHit(hit.entity()) || deflected) {
                        pendingRemove = true;
                        return;
                    }
                }
                // stop the forward move at the hit point so a bounce doesn't overshoot first
                if (deflectedThisTick) {
                    deflectedThisTick = false;
                    refreshPosition(hit.collisionPoint().asPos().withView(prevYaw, prevPitch), false, false);
                    return;
                }
            }
        }

        if (!world.isChunkLoaded(newPosition)) return;

        this.justBecameStuck = false;
        if (blockContact) {
            this.impactPosition = newPosition;
            for (int axis = 0; axis < 3; axis++) {
                if (physics.collisionShapes()[axis] instanceof ShapeImpl) {
                    Point hitPoint = physics.collisionPoints()[axis];
                    this.impactCell = physics.collisionShapePositions()[axis];
                    this.impactShape = physics.collisionShapes()[axis];
                    Block hitBlock = impactCell != null
                            ? world.getBlock(impactCell, Block.Getter.Condition.TYPE)
                            : world.getBlock(hitPoint.sub(0, Vec.EPSILON, 0), Block.Getter.Condition.TYPE);
                    stick(hitBlock, hitPoint, axis, newPosition);
                    if (isRemoved() || pendingRemove) return;
                    break;
                }
            }
        } else if (physics.hasCollision()) {
            // clipped move without contact: vanilla move() semantics - collided axes zero, the slide keeps the rest
            velocityBt = physics.newVelocity();
            this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
            onBlockClip(physics);
        }

        // 1.8 applies drag + gravity after the move (skip on the stick tick - velocity is already zeroed + frozen)
        if (physicsOrder == ProjectileTypeConfig.PhysicsOrder.DRAG_AFTER_MOVE && !justBecameStuck && !coasting()) applyDragGravity(nativeStep);
        this.onGround = physics.isOnGround();
        float yaw = prevYaw, pitch = prevPitch;
        if (justBecameStuck) {
            yaw = stuckYaw;
            pitch = stuckPitch;
        } else {
            Vec displacement = newPosition.sub(position).asVec();
            if (displacement.lengthSquared() > 1e-8) {
                yaw = lerpRotation(prevYaw, Directions.yaw(displacement));
                pitch = lerpRotation(prevPitch, Directions.pitch(displacement));
            }
        }
        this.prevYaw = yaw;
        this.prevPitch = pitch;

        // the physics-resolved position, not the collision point - modern clients saw the projectile float in
        // front of the block face
        Pos place = justBecameStuck && stuckPlacement != null ? stuckPlacement : newPosition;
        stepHistory.addFirst(position);
        if (stepHistory.size() > 64) stepHistory.removeLast();
        refreshPosition(place.withView(yaw, pitch), false, broadcastMovement);
        // frozen stick only: resyncStuck owns the broadcast. A live halt keeps lastSynced so the send threshold accumulates.
        if (justBecameStuck && isStuck()) this.lastSyncedPosition = getPosition();

        // 1.8 tracker m=0 pass: one velocity after the first physics tick
        if (gravityTickCount == 1 && velocitySyncInterval > 0 && !isStuck()) {
            broadcastWireVelocity();
        }
    }

    /** One tick of air resistance + gravity on {@link #velocityBt}, before or after the move per {@link #physicsOrder}. */
    private void applyDragGravity(boolean nativeStep) {
        Aerodynamics aero = getAerodynamics();
        // a sub-step IS a vanilla tick, so it uses the raw constants; the coarse path stretches them instead
        // (drag^(clientTps/serverTps), gravity × (clientTps/serverTps)²). Both are identity at the native rate.
        boolean wet = inWater && waterDrag < 1.0;
        double baseH = wet ? waterDrag : aero.horizontalAirResistance();
        double baseV = wet ? waterDrag : aero.verticalAirResistance();
        double hDrag = nativeStep ? baseH : TickScaler.dragPerTick(scopeSubject(), baseH);
        double vDrag = nativeStep ? baseV : TickScaler.dragPerTick(scopeSubject(), baseV);
        double gravity = hasNoGravity() ? 0
                : nativeStep ? aero.gravity() : TickScaler.gravityPerTick(scopeSubject(), aero.gravity());
        Vec accel = acceleration.mul(nativeStep ? 1.0 : TickScaler.gravityPerTick(scopeSubject(), 1.0)); // thrust scales like gravity
        velocityBt = physicsOrder == ProjectileTypeConfig.PhysicsOrder.DRAG_BEFORE_MOVE
                ? velocityBt.add(accel).sub(0, gravity, 0).mul(hDrag, vDrag, hDrag)
                : velocityBt.add(accel).mul(hDrag, vDrag, hDrag).sub(0, gravity, 0);
        this.velocity = velocityBt.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    /** The 26.1 {@link #leftOwnerImmunity} test (vanilla {@code Projectile.isOutsideOwnerCollisionRange}). */
    private boolean withinShooterBox(Point point) {
        if (shooter == null) return false;
        var bb = shooter.getBoundingBox();
        Pos sp = shooter.getPosition();
        double g = LEFT_OWNER_INFLATE;
        return point.x() >= sp.x() + bb.relativeStart().x() - g && point.x() <= sp.x() + bb.relativeEnd().x() + g
                && point.y() >= sp.y() + bb.relativeStart().y() - g && point.y() <= sp.y() + bb.relativeEnd().y() + g
                && point.z() >= sp.z() + bb.relativeStart().z() - g && point.z() <= sp.z() + bb.relativeEnd().z() + g;
    }

    /** Vanilla {@code Projectile.lerpRotation}: ease {@code from} toward {@code target} by {@link #ROTATION_LERP}, shortest-arc (degrees wrap). */
    private static float lerpRotation(float from, float target) {
        while (target - from < -180f) from -= 360f;
        while (target - from >= 180f) from += 360f;
        return from + (target - from) * ROTATION_LERP;
    }

    private void stick(Block hitBlock, Point hitPoint, int hitAxis, Pos resolvedPosition) {
        var event = new ProjectileCollideWithBlockEvent(this, hitPoint.asPos(), hitBlock);
        EventDispatcher.call(event);
        if (event.isCancelled()) return;

        // latch the stuck render rotation + face normal while velocity is still the flight value
        if (velocityBt.lengthSquared() > 1e-8) {
            stuckYaw = lerpRotation(prevYaw, Directions.yaw(velocityBt));
            stuckPitch = lerpRotation(prevPitch, Directions.pitch(velocityBt));
        } else {
            stuckYaw = prevYaw;
            stuckPitch = prevPitch;
        }
        Vec normal = switch (hitAxis) {
            case 0 -> new Vec(Math.signum(velocityBt.x()), 0, 0);
            case 1 -> new Vec(0, Math.signum(velocityBt.y()), 0);
            case 2 -> new Vec(0, 0, Math.signum(velocityBt.z()));
            default -> velocityBt.normalize();
        };
        this.impactFace = switch (hitAxis) { // before onStuck(): the impact pipeline runs inside it
            case 0 -> velocityBt.x() > 0 ? BlockFace.WEST : BlockFace.EAST;
            case 1 -> velocityBt.y() > 0 ? BlockFace.BOTTOM : BlockFace.TOP;
            case 2 -> velocityBt.z() > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
            default -> null;
        };

        // onStuck() returns removeOnBlockHit: true = break (throwables), false = stick (arrow). Decide before entering the stuck state.
        boolean breakOnHit = onStuck();
        if (isRemoved()) return;
        if (breakOnHit) {
            setVelocityBt(Vec.ZERO);
            pendingRemove = true;
            return;
        }

        Vec flightDir = velocityBt.lengthSquared() > 1e-8 ? velocityBt.normalize() : normal;
        this.stuckPlacement = stickPlacement(resolvedPosition, flightDir);
        this.justBecameStuck = true; // skips this tick's drag/gravity + places at stuckPlacement (vanilla skips the move)
        if (!freezeOnStick()) return; // live halt: velocity kept, keeps ticking - the subclass owns the contact response

        this.collisionDirection = normal;
        this.stuckCollisionPoint = hitPoint;
        this.stuckSyncCounter = 0;
        setNoGravity(true);
        setVelocityBt(Vec.ZERO);
    }

    /** Block-contact response: {@code true} (default) = the frozen stuck state (arrow). {@code false} = halt in place
     *  this tick but stay live with velocity kept - the 1.8 bobber's contact, which damp-settles instead of freezing. */
    protected boolean freezeOnStick() { return true; }

    /** True while the projectile flies at constant velocity (no drag, gravity or acceleration) - the fireball's launch coast. */
    protected boolean coasting() { return false; }

    /** Box for the block-clipped move; default = {@link #collisionBox()}. The bobber's vanilla dual model: contact
     *  detection stays the point ray while the real 0.25 box clips the move. */
    protected BoundingBox moveBox() { return collisionBox(); }

    /** Whether a block collision of the contact sweep {@link #stick sticks}; {@code false} = contact never sticks and
     *  the clipped move responds via {@link #onBlockClip} (the 26.1 hook has no stuck state at all). */
    protected boolean stickOnBlockContact() { return true; }

    /** The move clipped a block without sticking: collided axes are already zeroed, the slide kept. */
    protected void onBlockClip(PhysicsResult physics) {}

    /** The physics-resolved position pulled back {@link #stickPullback} along flight so the tip pokes out of the
     *  block face (vanilla arrow). The bobber overrides it - 1.8 freezes it pre-move. */
    protected Pos stickPlacement(Pos resolvedPosition, Vec flightDir) {
        return resolvedPosition.sub(flightDir.mul(stickPullback));
    }

    private boolean shouldUnstuck() {
        if (collisionDirection == null || stuckCollisionPoint == null) return false;
        // block broken = unstick (an intersect-box check false-unsticks on fences/slabs)
        Point intoBlock = stuckCollisionPoint.add(collisionDirection.mul(0.5));
        MechanicsWorld world = MechanicsWorld.of(this);
        // don't unstick while the chunk is unloaded (a relog briefly empties it -> reads AIR -> drops the arrow)
        if (!world.isChunkLoaded(intoBlock)) return false;
        return world.getBlock(intoBlock.asBlockVec(), Block.Getter.Condition.TYPE).isAir();
    }

    private void unstick() {
        EventDispatcher.call(new ProjectileUncollideEvent(this));
        collisionDirection = null;
        stuckCollisionPoint = null;
        setNoGravity(false);
        onUnstuck();
        // a relogged 1.8 client may briefly "freeze then fall" (it holds inGround until its block-changed check) - vanilla too
    }

    // per-server-tick displacement: rate steps above the server rate (sub-stepping), one at/below it
    private Vec displacementBt() {
        double rate = TickScaler.stepsPerTick(scopeSubject());
        return rate > 1.0 ? velocityBt.mul(rate) : velocityBt;
    }

    // sign-preserving |motY| floor, clamping exactly-0 up; the sim flies the true arc regardless
    private Vec floorMotY(Vec v) {
        if (wireMotYFloor > 0 && Math.abs(v.y()) < wireMotYFloor && !v.isZero()) {
            return v.withY(v.y() < 0 ? -wireMotYFloor : wireMotYFloor);
        }
        return v;
    }

    /** MODERN scope-rated wire velocity (ZERO while stuck); a 1.8 client needs {@link #legacyVelocityForPacket}. */
    @Override
    protected Vec getVelocityForPacket() {
        return isStuck() ? Vec.ZERO : floorMotY(TickScaler.wireVelocity(scopeSubject(), displacementBt()));
    }

    /** 1.8 clients tick at the server rate regardless of dilation, so they take the raw step; the scope-rated
     *  {@link #getVelocityForPacket} would overshoot them by 20/clientTps in slow motion. Equal off dilation. */
    private Vec legacyVelocityForPacket() {
        return isStuck() ? Vec.ZERO : floorMotY(displacementBt());
    }

    protected Vec wireVelocityFor(@NotNull Player viewer) {
        return Polyp.getInstance().clientInfo().isLegacy(viewer)
                ? legacyVelocityForPacket() : getVelocityForPacket();
    }

    @FunctionalInterface
    private interface WirePacket { SendablePacket build(int entityId, Vec velocity); }

    /** Per-viewer velocity send: the sim advances once per server tick but each viewer reads it at its own tick rate,
     *  so a 1.8 client gets the raw step and a modern one the scope-rated step. Bare, not a shared CachedPacket that
     *  would drop the per-viewer split ([[grouped-packet-defeats-per-viewer-rewrites]]). */
    private void sendPerViewerVelocity(WirePacket packet) {
        Vec modern = getVelocityForPacket(), legacy = legacyVelocityForPacket();
        var clientInfo = Polyp.getInstance().clientInfo();
        for (Player viewer : getViewers()) {
            viewer.sendPacket(packet.build(getEntityId(), clientInfo.isLegacy(viewer) ? legacy : modern));
        }
    }

    private @Nullable Vec lastBroadcastBt;

    // on-change (vanilla-like): skip identical re-sends, so a constant-velocity phase (the fireball coast) broadcasts once
    private void broadcastWireVelocity() {
        if (velocityBt.equals(lastBroadcastBt)) return;
        lastBroadcastBt = velocityBt;
        sendPerViewerVelocity((id, vel) -> new EntityVelocityPacket(id, vel));
    }

    /** Vanilla tracker send gate (EntityTrackerEntry): a settling/rested projectile otherwise yanks the predicting
     *  client with no-op corrections. */
    private static final double SYNC_SEND_THRESHOLD = 4 / 32.0;
    /** Vanilla {@code m % 60} keepalive: a position update at least this often even below the threshold. */
    private static final int SYNC_KEEPALIVE_TICKS = 60;
    private long lastSyncSendTick;

    // absolute-teleport position sync (Minestom's relative-move is invisible to 1.8 via Via): a sparse absolute teleport
    // every updateInterval, client predicts the arc between.
    @Override
    protected void synchronizePosition() {
        if (pendingRemove) return; // hit this tick and about to be removed: no final position/velocity broadcast
        // externally driven (a replay parks it: no gravity, zero own velocity - stuck included): the driver
        // moves it through refreshPosition, whose sends flow only while the base schedule is armed; the
        // interval-0 silent wire never rearms it, so without the super call every relative move swallows
        if (hasNoGravity() && velocityBt.isZero()) {
            if (getSynchronizationTicks() <= 1) setSynchronizationTicks(ServerFlag.ENTITY_SYNCHRONIZATION_TICKS);
            super.synchronizePosition();
            return;
        }
        // while stuck, resyncStuck() owns the broadcast; don't send on the stick tick (the resync's next teleport is the correction)
        if (isStuck()) {
            this.lastSyncedPosition = getPosition();
            return;
        }
        if (getSynchronizationTicks() <= 0) return; // silent wire (syncInterval 0): spawn-predicted, event-driven broadcasts only
        // throttle to syncInterval (a per-tick teleport shakes both clients - it fights their interpolation/prediction)
        if (flightSyncCounter++ % Math.max(1L, getSynchronizationTicks()) != 0) return;
        Pos pos = getPosition();
        if (Math.abs(pos.x() - lastSyncedPosition.x()) < SYNC_SEND_THRESHOLD
                && Math.abs(pos.y() - lastSyncedPosition.y()) < SYNC_SEND_THRESHOLD
                && Math.abs(pos.z() - lastSyncedPosition.z()) < SYNC_SEND_THRESHOLD
                && getAliveTicks() - lastSyncSendTick < TickScaler.clientTicks(scopeSubject(), SYNC_KEEPALIVE_TICKS)) return;
        lastSyncSendTick = getAliveTicks();
        // Via re-emits the teleport's velocity as a 1.8 entity_velocity: live value = vanilla's correction pair,
        // ZERO reaches 1.8 as a spurious 0,0,0. A denser velocity stream (fireball) keeps it out - Via would duplicate.
        boolean carriesVelocity = velocitySyncInterval <= 0 || velocitySyncInterval >= getSynchronizationTicks();
        if (carriesVelocity) {
            // Via re-emits the teleport's velocity as a 1.8 entity_velocity, so the per-viewer split rides it
            sendPerViewerVelocity((id, vel) -> new EntityTeleportPacket(id, pos, vel, 0, onGround));
        } else {
            sendPacketToViewersAndSelf(new EntityTeleportPacket(getEntityId(), pos, Vec.ZERO, 0, onGround));
        }
        if (carriesVelocity && velocitySyncInterval > 0) velocityCarried = true; // eat the same-tick standalone
        this.lastSyncedPosition = pos;
    }

    // re-assert absolute pos + rotation periodically so a stuck 1.8 client self-heals mispredictions; a no-op for the modern (inGround) client
    private void resyncStuck() {
        Pos pos = getPosition();
        sendPacketToViewersAndSelf(new EntityTeleportPacket(getEntityId(), pos, Vec.ZERO, 0, true));
        sendVelocityToViewers(Vec.ZERO); // one-time zero when the arrow stops
        this.lastSyncedPosition = pos;
    }

    /** True only around deliberate velocity broadcasts, so {@link #sendPacketToViewers} lets them through. */
    private boolean allowVelocityPacket;

    /** The sync teleport carried the live velocity this tick; the standalone that follows would be Via-duplicated. */
    private boolean velocityCarried;

    private void sendVelocityToViewers(@NotNull Vec velocity) {
        allowVelocityPacket = true;
        try {
            sendPacketToViewersAndSelf(new EntityVelocityPacket(getEntityId(), velocity));
        } finally {
            allowVelocityPacket = false;
        }
    }

    /** The block-edge "slide" fix: Minestom's per-tick velocity re-broadcast perturbs the 1.8 client's spawn-velocity
     *  prediction over Via, so drop it (gated by {@code velocitySyncInterval}). See design doc §3f. */
    @Override
    public void sendPacketToViewers(@NotNull SendablePacket packet) {
        if (packet instanceof EntityVelocityPacket && !allowVelocityPacket) {
            if (pendingRemove) return;             // being removed this tick: no final velocity broadcast
            // externally driven (see synchronizePosition): fully silent except deliberate broadcasts
            if (hasNoGravity() && velocityBt.isZero()) return;
            if (velocitySyncInterval <= 0) return; // vanilla arrow: no per-tick velocity, client predicts from spawn
            if (autoVelocityCounter++ % velocitySyncInterval != 0) return; // else every velocitySyncInterval ticks
            // after the counter: the eaten send still consumes its slot
            if (velocityCarried) { velocityCarried = false; return; }
            broadcastWireVelocity(); // Minestom's packet holds one (modern) velocity; re-send it per-viewer instead
            return;
        }
        super.sendPacketToViewers(packet);
    }

    // new viewer (spawn, relog, chunk-cross). a stuck arrow spawns with zero velocity for both clients (getVelocityForPacket),
    // then modern holds via inGround and 1.8 self-heals from resyncStuck()
    @Override
    public void updateNewViewer(@NotNull Player player) {
        int data = shooter != null ? shooter.getEntityId() : 0;
        // viewers attach after the first move: the spawn packet must carry the launch position (the client predicts from it)
        Pos pos = getAliveTicks() <= 1 && spawnPosition != null ? spawnPosition : getPosition();
        player.sendPacket(new SpawnEntityPacket(getEntityId(), getUuid(), getEntityType(), pos, pos.yaw(), data, wireVelocityFor(player)));
        player.sendPacket(getMetadataPacket());
        // no spawn velocity dup: the Via chain already re-emits it as the 1.8 S12
    }


    // vanilla impact poof, the 1.8-server way: REAL particle packets (EntitySnowball.onImpact spawns
    // 8; pearls 32 PORTAL) - both client generations render them, and they record as replay FX.
    // 26.1 uses entity event 3 instead, which 1.8 clients ignore
    private void breakParticles() {
        var type = getEntityType();
        Particle particle;
        float offset;
        int count;
        if (type == EntityType.SNOWBALL) {
            particle = Particle.ITEM_SNOWBALL;
            offset = 0f;
            count = 8;
        } else if (type == EntityType.EGG) {
            particle = Particle.ITEM.withItem(ItemStack.of(Material.EGG));
            offset = 0.05f;
            count = 8;
        } else if (type == EntityType.ENDER_PEARL) {
            particle = Particle.PORTAL;
            offset = 0.5f;
            count = 32;
        } else {
            return;
        }
        var pos = getPosition();
        var packet = new ParticlePacket(particle, pos.x(), pos.y(), pos.z(), 0f, 0f, 0f, offset, count);
        // 1.8-line sims self-predict the poof (1.8 onImpact particles are not isRemote-gated, and
        // Animatium's 1.8 mechanics do the same) - packets would double them. Replays are unaffected:
        // recorded FX re-broadcasts reach twins, whose starved sims never collide
        var clientInfo = io.github.term4.polyp.Polyp.getInstance().clientInfo();
        for (var viewer : getViewers()) {
            if (clientInfo.isLegacy(viewer)) continue;
            if (viewer instanceof io.github.term4.polyp.platform.player.OptimizedPlayer op
                    && op.compat().isAnimatiumClient()) continue;
            viewer.sendPacket(packet);
        }
    }
}
