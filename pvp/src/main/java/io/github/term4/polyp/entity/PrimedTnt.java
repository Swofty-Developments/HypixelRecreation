package io.github.term4.polyp.entity;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.ExplosionEvent;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.mechanics.explosion.ExplosionCalculator;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.world.ExternallyTickable;
import io.github.term4.polyp.world.MechanicsWorld;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityPositionSyncPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Primed TNT running 1.8 physics by hand (gravity before the move, then drag; grounded = ×0.7 friction + the
 * motY×-0.5 bounce). {@link Config} carries the preset knobs; the wire is hand-sent per {@link Wire}; a TNT source's
 * push on other TNT rescales per {@code tntVictimScale} ({@link #retuneTntVictims}).
 */
public final class PrimedTnt extends Entity implements ExternallyTickable {

    /**
     * {@code detonateAtFeet}: MineMen/Hypixel at the feet, vanilla at {@code +height/16}. {@code bounce}: the motY×-0.5
     * ground rebound (mmc18: hard impacts bounce, soft landings rest); false lands flat (Hypixel). {@code tntVictimScale}:
     * the absolute push scale this TNT's blast applies to other primed TNT, overriding the profile's player/fireball KB
     * multiplier (MineMen's TNT-on-TNT ~1.1 is weaker than its KB_SCALE fireball push); null = none (the profile's scale).
     */
    public record Config(int fuseTicks, float power, boolean detonateAtFeet, Wire wire, boolean bounce, Double tntVictimScale) {}

    /**
     * Measured tracker cadences. {@code MINEMEN}: etp+vel on 10t boundaries while moving, silent at rest, grounded
     * tracker vy floored +0.05, blast impulse sent raw on delivery. {@code HYPIXEL}: velocity every 3t while
     * falling, one position sync at tick 3, and (like vanilla) a blast impulse broadcast post-friction.
     */
    public enum Wire { MINEMEN, HYPIXEL }

    private static final int MINEMEN_SYNC_TICKS = 10;
    private static final double MINEMEN_VY_FLOOR = 0.05;
    private static final int HYPIXEL_VELOCITY_INTERVAL = 3;
    private static final int HYPIXEL_TELEPORT_TICK = 3;
    private static final int LEGACY_SELF_FUSE = 80; // a 1.8 client counts this many ticks itself and setDead()s at zero
    private static final int LEGACY_REARM_INTERVAL = 60; // re-send the spawn under that window to restart the count
    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;
    private static final AtomicBoolean RETUNE_INSTALLED = new AtomicBoolean();

    private final ExplosionSystem explosion;
    private final Config config;
    private int fuse;
    private boolean fuseScaled;
    private boolean fuseOutlivesLegacyCount; // a fuse longer than the 1.8 client's own ~80t self-count needs re-arming
    private int rearmAge;
    private boolean spawnVelocityPendingScale; // the scatter is set pre-world; re-rate it once the scope resolves
    private Vec motion = Vec.ZERO; // b/t; the 1.8 pipeline runs on this, Minestom's per-tick result is overwritten
    private Point wireSyncedAt;
    private boolean rawBroadcast;
    private boolean flipPending;
    private boolean pushed; // a blast impulse awaiting its post-friction broadcast (HYPIXEL wire)
    private int flightTick;
    private boolean wasAirborne = true;

    private PrimedTnt(ExplosionSystem explosion, Config config) {
        super(EntityType.TNT);
        this.explosion = explosion;
        this.config = config;
        this.fuse = config.fuseTicks();
        // the copy keeps the REMAINING fuse + current motion (world fork/respawn cloners read the stamp)
        setTag(MechanicsWorld.ENTITY_COPY, () -> {
            PrimedTnt copy = new PrimedTnt(explosion, config);
            copy.fuse = fuse;
            copy.motion = motion;
            return copy;
        });
        setTag(MechanicsWorld.ENTITY_SAVE, () -> {
            CompoundBinaryTag.Builder out = CompoundBinaryTag.builder().putString("id", "polyp:tnt")
                    .putInt("fuse", fuse)
                    .putInt("fuseTicks", config.fuseTicks())
                    .putFloat("power", config.power())
                    .putBoolean("feet", config.detonateAtFeet())
                    .putString("wire", config.wire().name())
                    .putBoolean("bounce", config.bounce())
                    .put("vel", ListBinaryTag.builder(BinaryTagTypes.DOUBLE)
                            .add(DoubleBinaryTag.doubleBinaryTag(motion.x()))
                            .add(DoubleBinaryTag.doubleBinaryTag(motion.y()))
                            .add(DoubleBinaryTag.doubleBinaryTag(motion.z())).build());
            if (config.tntVictimScale() != null) out.putDouble("victimScale", config.tntVictimScale());
            return out.build();
        });
        installRetune();
        setTag(ProjectileEntity.PROJECTILE_COLLIDABLE, true); // fireballs detonate on it, arrows deflect off
        // sync is hand-sent in update(); Minestom's scheduled sync would re-send forever at rest and flip the
        // vel/pos wire order. setSynchronizationTicks does NOT reset the seeded tick-20 first sync - clear it too.
        setSynchronizationTicks(config.fuseTicks() + 20L);
        try {
            var latch = Entity.class.getDeclaredField("nextSynchronizationTick");
            latch.setAccessible(true);
            latch.setLong(this, Long.MAX_VALUE);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // data=1 + real velocity: a data-0 1.8 spawn can't carry velocity, so ViaRewind splits it into a scheduled
    // SET_ENTITY_MOTION whose zero lands mid-hop and kills the kick prediction (both clients ignore the data value for TNT)
    @Override
    protected SpawnEntityPacket getSpawnPacket() {
        Pos position = getPosition();
        return new SpawnEntityPacket(getEntityId(), getUuid(), getEntityType(), position, position.yaw(), 1, getVelocityForPacket());
    }

    /** The load-side reviver for {@code "polyp:tnt"} descriptors: remaining fuse + motion + preset knobs.
     *  A revived twin runs the REAL physics and hand wire - the live bounce look on every client. */
    public static PrimedTnt fromSave(ExplosionSystem explosion, CompoundBinaryTag data) {
        Config config = new Config(data.getInt("fuseTicks"), data.getFloat("power"), data.getBoolean("feet"),
                Wire.valueOf(data.getString("wire")), data.getBoolean("bounce"),
                data.get("victimScale") != null ? data.getDouble("victimScale") : null);
        PrimedTnt tnt = new PrimedTnt(explosion, config);
        tnt.fuse = data.getInt("fuse");
        ListBinaryTag vel = data.getList("vel", BinaryTagTypes.DOUBLE);
        if (vel.size() == 3) tnt.motion = new Vec(vel.getDouble(0), vel.getDouble(1), vel.getDouble(2));
        tnt.velocity = tnt.motion.mul(TPS);
        ((PrimedTntMeta) tnt.getEntityMeta()).setFuseTime(tnt.fuse);
        return tnt;
    }

    /** A replay twin never detonates: the recording's REMOVE + FX carry the explosion (client fuse untouched). */
    public void sterilize() {
        fuse = Integer.MAX_VALUE;
    }

    public int remainingFuseTicks() {
        return fuse;
    }

    /** Spawns at the block's {@code +0.5,+0.5,+0.5} (measured) with the vanilla kick. */
    public static PrimedTnt spawn(ExplosionSystem explosion, Instance instance, Point tntBlock, Config config) {
        return spawn(explosion, MechanicsWorld.of(instance), tntBlock, config);
    }

    /** MechanicsWorld-bound spawn: the TNT belongs to {@code shard} (visibility + its blast targets/exposure/broadcast). */
    public static PrimedTnt spawn(ExplosionSystem explosion, MechanicsWorld shard, Point tntBlock, Config config) {
        PrimedTnt tnt = new PrimedTnt(explosion, config);
        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
        tnt.setVelocity(new Vec(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02).mul(TPS));
        shard.spawn(tnt, new Pos(tntBlock.blockX() + 0.5, tntBlock.blockY() + 0.5, tntBlock.blockZ() + 0.5));
        tnt.wireSyncedAt = tnt.getPosition();
        var polyp = Polyp.getInstance();
        if (polyp.isInitialized()) Fx.play(polyp.services(), Fx.TNT_PRIME, FxContext.of(tnt));
        return tnt;
    }

    /** A blast impulse joins the 1.8 pipeline. MineMen sends it raw immediately; the HYPIXEL/vanilla wire defers to the post-friction send in {@link #update}. */
    @Override
    public void setVelocity(@NotNull Vec velocity) {
        this.motion = velocity.div(TPS);
        flipPending = isOnGround() && motion.y() < 0; // a downward impulse into a grounded TNT bounces at any magnitude
        if (getInstance() == null) {
            // spawn scatter, pre-world: re-rate this native-rate velocity to the scope's sim rate on the first tick,
            // else gravity's ×s² over-launches it. moveVector here resolves scaling with no instance and poisons the tick-0 memo.
            spawnVelocityPendingScale = true;
            return;
        }
        if (config.wire() == Wire.HYPIXEL) {
            pushed = true;
        } else {
            rawBroadcast = true; // MineMen: raw, unfloored on-blast send
            super.setVelocity(velocity);
            rawBroadcast = false;
        }
        this.velocity = moveVector();
    }

    /** Vanilla motY state (the wire-visible value), not the Minestom move vector. */
    @Override
    public @NotNull Vec getVelocity() {
        return motion.mul(TPS);
    }

    private PhysicsResult lastPhysics;

    // an externally ticked TNT in the global dispatcher double-ticks: skip the field write's re-registration
    @Override protected void refreshCurrentChunk(@NotNull Chunk chunk) {
        if (MechanicsWorld.externallyTicked(this)) {
            currentChunk = chunk;
            return;
        }
        super.refreshCurrentChunk(chunk);
    }

    // bail on a foreign clock, or the fuse counts on both and detonates early (movementTick + update ride super.tick)
    @Override public void tick(long time) {
        if (!MechanicsWorld.ownsCurrentTick(this)) return;
        super.tick(time);
    }

    @Override
    protected void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle != null || getInstance() == null) return;
        if (spawnVelocityPendingScale) { // first tick with a resolvable scope, before the physics step
            spawnVelocityPendingScale = false;
            motion = TickScaler.fromClientVelocity(this, motion);
            this.velocity = moveVector();
        }
        this.lastPhysics = MechanicsWorld.step(this, velocity.div(TPS), lastPhysics, result -> {
            this.velocity = result.newVelocity().mul(TPS);
            this.onGround = result.isOnGround();
            refreshPosition(result.newPosition(), true, false); // TNT is a synchronize-only type: the wire is hand-sent
        });
    }

    // vanilla applies gravity BEFORE the move; handing Minestom the raw motion over-shoots the hop apex (+0.577 vs +0.386)
    private Vec moveVector() {
        return motion.sub(0, TickScaler.aerodynamics(this, getAerodynamics()).gravity(), 0).mul(TPS);
    }

    @Override
    public void update(long time) {
        // 1.8 TNT tick on our own motion: gravity before the move, drag, then the grounded ×0.7 friction + bounce.
        Aerodynamics aero = TickScaler.aerodynamics(this, getAerodynamics());
        double vy = (motion.y() - aero.gravity()) * aero.verticalAirResistance();
        double friction = isOnGround() ? TickScaler.dragPerTick(this, 0.7) : 1.0;
        if (isOnGround()) vy = groundVy(vy);
        flipPending = false;
        double hDrag = aero.horizontalAirResistance() * friction;
        motion = new Vec(motion.x() * hDrag, vy, motion.z() * hDrag);
        this.velocity = moveVector();

        if (wireSyncedAt == null) wireSyncedAt = getPosition(); // a revived twin skips spawn()'s wire init
        if (pushed) { // vanilla broadcasts a blast impulse post-friction (its tracker phase), replacing this tick's cadence send
            pushed = false;
            sendPacketToViewersAndSelf(getVelocityPacket());
        } else switch (config.wire()) {
            case MINEMEN -> {
                long t = getAliveTicks();
                if (t >= MINEMEN_SYNC_TICKS && t % MINEMEN_SYNC_TICKS == 0) minemenSyncIfMoved();
            }
            case HYPIXEL -> hypixelWire();
        }
        wasAirborne = !isOnGround();

        // the ctor runs before the entity has a world, so the scope is only resolvable here; stretch once
        if (!fuseScaled) {
            fuseScaled = true;
            fuse = TickScaler.duration(this, fuse, ExplosionSystem.KEY);
            fuseOutlivesLegacyCount = fuse > LEGACY_SELF_FUSE;
            ((PrimedTntMeta) getEntityMeta()).setFuseTime(fuse);
        }
        // legacy clients hardcode an 80t fuse + self-setDead, so a stretched fuse vanishes early on them: re-arm
        // under that window. No grow anim - 1.8 can't dilate, and Hypixel's 50t fuse never reaches the grow phase.
        if (fuseOutlivesLegacyCount && ++rearmAge % LEGACY_REARM_INTERVAL == 0) rearmLegacyViewers();
        if (--fuse > 0) return;
        Instance instance = getInstance();
        if (instance != null) explosion.explode(MechanicsWorld.of(this, instance), detonationCenter(), config.power(), this, null);
        remove();
    }

    // removeViewer no-ops on an auto-viewable entity (the viewer isn't a manual one), so drive the destroy + spawn
    // directly - a fresh spawn restarts the client's own fuse count.
    private void rearmLegacyViewers() {
        for (Player viewer : List.copyOf(getViewers())) {
            if (viewer instanceof OptimizedPlayer op && op.compat().legacyClient()) {
                updateOldViewer(viewer);
                updateNewViewer(viewer);
            }
        }
    }

    // grounded vertical: a hard impact or a downward impulse into a grounded TNT bounces (×rebound); the first landing tick zeroes; upward passes
    private double groundVy(double vy) {
        double rebound = config.bounce() ? -0.5 : 0.0;
        if (vy < -0.6 || (flipPending && vy < 0)) return vy * rebound;
        if (vy > 0) return vy;
        return wasAirborne ? 0.0 : vy * rebound;
    }

    private Point detonationCenter() {
        return config.detonateAtFeet() ? getPosition() : getPosition().add(0, getBoundingBox().height() / 16.0, 0);
    }

    // 1.8 tracker gate: sync only when the fixed-point wire position moved
    private void minemenSyncIfMoved() {
        Pos pos = getPosition();
        if (wireSyncedAt != null && Math.abs(pos.x() - wireSyncedAt.x()) < 1.0 / 32
                && Math.abs(pos.y() - wireSyncedAt.y()) < 1.0 / 32 && Math.abs(pos.z() - wireSyncedAt.z()) < 1.0 / 32) return;
        sendSync(pos);
    }

    // one position sync a few ticks after spawn + velocity every few ticks while falling (the 1.8 client predicts
    // the arc; Via drops relative moves), then a final velocity on landing
    private void hypixelWire() {
        boolean airborne = !isOnGround();
        if (airborne) {
            if (flightTick == HYPIXEL_TELEPORT_TICK) sendSync(getPosition());
            else if (flightTick % HYPIXEL_VELOCITY_INTERVAL == 0) sendPacketToViewersAndSelf(getVelocityPacket());
            flightTick++;
        } else if (wasAirborne) {
            sendPacketToViewersAndSelf(getVelocityPacket());
        }
    }

    private void sendSync(Pos pos) {
        Point delta = wireSyncedAt == null ? Vec.ZERO : pos.sub(wireSyncedAt);
        wireSyncedAt = pos;
        sendPacketToViewers(new EntityPositionSyncPacket(getEntityId(), pos, delta, pos.yaw(), pos.pitch(), isOnGround()));
        sendPacketToViewers(getVelocityPacket());
    }

    /** MineMen's grounded tracker velocity floors vy at +0.05; the sim is untouched. */
    @Override
    protected Vec getVelocityForPacket() {
        Vec v = motion;
        return config.wire() == Wire.MINEMEN && !rawBroadcast && isOnGround() && v.y() < MINEMEN_VY_FLOOR
                ? v.withY(MINEMEN_VY_FLOOR) : v;
    }

    // A TNT source rescales its blast on OTHER primed TNT to config.tntVictimScale (absolute, feet-radial), overriding
    // the profile's player/fireball KB multiplier - MineMen's TNT-on-TNT (~1.1) is weaker than its KB_SCALE fireball
    // push. Fireball sources aren't PrimedTnt, so they pass through at the profile scale. Installed once, globally.
    private static void installRetune() {
        if (RETUNE_INSTALLED.compareAndSet(false, true))
            MinecraftServer.getGlobalEventHandler().addListener(ExplosionEvent.class, PrimedTnt::retuneTntVictims);
    }

    private static void retuneTntVictims(ExplosionEvent e) {
        if (!(e.source() instanceof PrimedTnt src) || src.config.tntVictimScale() == null) return;
        double scale = src.config.tntVictimScale();
        for (ExplosionEvent.Target target : e.targets()) {
            Vec push = target.knockback();
            if (!(target.entity() instanceof PrimedTnt) || push == null || push.lengthSquared() < 1.0e-12) continue;
            double impact = ExplosionCalculator.impact(target.distance(), e.power(), target.exposure());
            target.setKnockback(push.normalize().mul(scale * impact)); // keep the radial dir, replace the profile-scaled magnitude
        }
    }
}
