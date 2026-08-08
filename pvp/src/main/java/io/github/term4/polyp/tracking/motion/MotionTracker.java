package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfiles;
import io.github.term4.polyp.tracking.Tracker;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.util.Directions;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.vehicle.PlayerInputs;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Single authority for an entity's ground/air timeline and server-tracked motion (the replica of vanilla
 * {@code motX/motY/motZ}), backing the reads {@link VelocityRule} composes. A {@link PlayerMoveEvent} listener anchors
 * the air clock to the client's jump packet and records each move-delta; a per-tick task forward-simulates the vertical
 * mot and the entity-push/flow residuals. Physics constants are read live; only the vanilla numbers Minestom omits
 * ({@link #SPRINT_IMPULSE}, the clamp) are fixed here.
 */
public final class MotionTracker implements Tracker {

    /** Server tick of the client's rising move-packet (its true jump tick); the gravity-arc anchor. */
    private static final Tag<Long> AIR_START_TICK = Tag.Transient("polyp:air-start-tick");
    private static final Tag<Boolean> LAUNCHED = Tag.Transient("polyp:launched");
    private static final Tag<Vec> MOVE_VELOCITY = Tag.Transient("polyp:move-velocity");
    private static final Tag<MovePrev> MOVE_PREV = Tag.Transient("polyp:move-prev");
    /** Instance tick of the last move packet; gates the motY sim under {@code motYOnMovePacket}. */
    private static final Tag<Long> LAST_MOVE_TICK = Tag.Transient("polyp:last-move-tick");
    private static final Tag<LaunchStamp> LAUNCH_STAMP = Tag.Transient("polyp:launch-stamp");
    /**
     * Server-side horizontal {@code motX/motZ} (b/t): the victim's own simulated motion (the {@code bF()} sprint-jump
     * boost) plus delivered non-melee knockback, not the player's WASD and not melee knockback - vanilla restores the
     * pre-KB velocity after a player-melee hit. Anchored at its last write, read lazily, bled per elapsed tick.
     */
    private static final Tag<MotState> MOT_H = Tag.Transient("polyp:mot-h");
    /** 1.8 {@code bL()} push residual: a {@code 0.05} separation impulse per overlapping tick. */
    private static final Tag<Vec> ENTITY_PUSH = Tag.Transient("polyp:entity-push");

    /** Water-flow residual, 3D: the falling-water down-term rides in Y. */
    private static final Tag<Vec> FLOW_PUSH = Tag.Transient("polyp:flow-push");
    /** Read lazily by {@link #frictionPerTick} for the in-water horizontal friction. */
    private static final Tag<FluidFlow.Model> FLOW_MODEL = Tag.Transient("polyp:flow-model");

    /**
     * Vanilla {@code motY}, forward-simulated per tick like {@code EntityLiving.m()}. Reproduces the emergent quirks
     * the analytic arc can't, e.g. an early-collide re-fall at touchdown.
     */
    private static final Tag<VertSim> VERT_SIM = Tag.Transient("polyp:vert-sim");

    /** Vanilla sprint-jump impulse ({@code bF()}: {@code motX -= sin(yaw)*0.2, motZ += cos(yaw)*0.2}). */
    public static final double SPRINT_IMPULSE = 0.2;
    /** The vanilla {@code move()} 1-tick collision sweep. */
    public static final int DEFAULT_GROUND_TICKS = 1;
    private static final double DEFAULT_BLOCK_FRICTION = 0.6;

    /** The extra physics vanilla applies beyond ground/air. */
    private enum Env { NORMAL, WATER, LAVA, WEB, LADDER, BUBBLE, HONEY }

    /** Absent = {@link Env#NORMAL}. */
    private static final Tag<Env> ENV = Tag.Transient("polyp:env");

    // water friction/gravity are per FluidFlow.Model; ladder climb-up + climbable detection are per ClimbModel
    private static final double LAVA_FRICTION = 0.5;
    private static final double WATER_VERTICAL = 0.8;
    private static final double LAVA_VERTICAL = 0.5;
    private static final double FLUID_GRAVITY = 0.02;   // lava motY gravity
    private static final double LADDER_CLAMP = 0.15;    // ladder motX/Z + fall-speed cap
    private static final double LEVITATION_BASE = 0.05; // motY target = 0.05*(level+1)
    private static final double LEVITATION_RATE = 0.2;
    private static final double SLOW_FALL_GRAVITY = 0.01;
    // 26-only, gated by modernBlockPhysics
    private static final double BUBBLE_UP_STEP = 0.06, BUBBLE_UP_CAP = 0.7;
    private static final double BUBBLE_DOWN_STEP = 0.03, BUBBLE_DOWN_CAP = -0.3;
    private static final double HONEY_SLIDE = -0.1274;  // wall-slide floor
    private static final double FLUID_SWIM_STEP = 0.04; // jump/sneak in a fluid
    private static final double LIQUID_EDGE_BUMP = 0.3; // swim into a wall with headroom
    private static final double BLOCK_SPEED_FACTOR = 0.4; // soul sand / honey
    private static final double FLOW_IMPULSE = 0.014;   // water current scale
    private static final double LAVA_FLOW_IMPULSE = 0.0023333333333333335; // 26 overworld lava current scale

    private static final Predicate<Block> IS_WATER = b -> b.compare(Block.WATER);
    private static final Predicate<Block> IS_LAVA = b -> b.compare(Block.LAVA);
    /** {@code makeStuckInBlock}: both 1.8 and 26 zero the tracked velocity here. */
    private static final Predicate<Block> IS_WEB = b -> b.compare(Block.COBWEB);
    /** The full 26 {@code makeStuckInBlock} family; sweet-berry/powder-snow are 26-only. */
    private static final Predicate<Block> IS_WEB_MODERN = b -> b.compare(Block.COBWEB)
            || b.compare(Block.SWEET_BERRY_BUSH) || b.compare(Block.POWDER_SNOW);
    private static final Predicate<Block> IS_HONEY = b -> b.compare(Block.HONEY_BLOCK);

    /** {@code null} if absent from the bundled registry - then a {@code _bed} key-suffix fallback. */
    private static final RegistryTag<Block> BEDS = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:beds"));

    private record MovePrev(double x, double y, double z, boolean onGround) {}
    private record LaunchStamp(long tick, double yaw, boolean sprinting, Vec residualH, Vec seedH) {}
    /** Horizontal residual as of {@code sinceTick}, bleeding by air or ground friction per the held state. */
    private record MotState(Vec motH, long sinceTick, boolean airborne) {}

    /**
     * Ticked {@code motY} state. {@code clamped} = vanilla (0.005 apex reseed); {@code raw} = no reseed ({@code clampY(0)}
     * presets). Index {@code [0]} = latest end-of-tick; older slots back the {@code launchOffset} lookback.
     */
    private static final class VertSim {
        static final int HISTORY = 4;
        final double[] clamped = new double[HISTORY];
        final double[] raw = new double[HISTORY];
        /** Last {@code move()} probe clamped a descent - vanilla's collision {@code onGround}. */
        boolean collided;
    }

    /** {@code seedH} = takeoff {@code motX/motZ}: {@code residualH} plus the {@link #SPRINT_IMPULSE} boost when sprinting. */
    public record JumpInfo(double yaw, boolean sprinting, Vec residualH, Vec seedH) {}

    private final MechanicsProfiles profiles;

    private static final AtomicBoolean CLOCK_RESET = new AtomicBoolean();

    public MotionTracker(MechanicsProfiles profiles) {
        this.profiles = profiles;
        if (CLOCK_RESET.compareAndSet(false, true)) {
            TickSystem.onClockChange(e -> {
                if (e instanceof Player p) clearTransient(p);
            });
        }
    }

    /**
     * {@link TickPhase#PRE_DISPATCH} - after the clock advances, before the dispatcher ticks entities - so the poll,
     * {@link #onMove}, and every residual read see one clock value per physical tick (same phase discipline as the
     * i-frame window and the hit queue).
     */
    @Override
    public void start() {
        TickSystem.register(TickPhase.PRE_DISPATCH, this::tick);
    }

    @Override
    public EventNode<@NotNull Event> node() {
        EventNode<@NotNull Event> node = EventNode.all("polyp:motion-tracker");
        node.addListener(PlayerMoveEvent.class, this::onMove);
        node.addListener(PlayerSpawnEvent.class, e -> clearTransient(e.getPlayer()));
        // same-instance teleport (pearl, /tp, shard hop): drop ONLY the position anchor, or the first post-teleport
        // move reconstructs the jump as one huge velocity. The client keeps its real motion, so the rest stays valid.
        node.addListener(EntityTeleportEvent.class, e -> { if (e.getEntity() instanceof Player p) p.removeTag(MOVE_PREV); });
        return node;
    }

    /** Reseeds from the next move packet; the clock-anchored stamps belong to the old instance's {@link TickSystem}. */
    public static void clearTransient(Player p) {
        p.removeTag(AIR_START_TICK);
        p.removeTag(LAUNCHED);
        p.removeTag(MOVE_VELOCITY);
        p.removeTag(MOVE_PREV);
        p.removeTag(LAST_MOVE_TICK);
        p.removeTag(LAUNCH_STAMP);
        p.removeTag(MOT_H);
        p.removeTag(ENTITY_PUSH);
        p.removeTag(FLOW_PUSH);
        p.removeTag(FLOW_MODEL);
        p.removeTag(VERT_SIM);
        p.removeTag(ENV);
    }

    private void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        long now = TickSystem.tick(p);
        p.setTag(LAST_MOVE_TICK, now);
        boolean nowOnGround = e.isOnGround();
        Pos newPos = e.getNewPosition();
        // compare vs the previous packet so transition detection is independent of event/move ordering
        MovePrev prev = p.getTag(MOVE_PREV);
        p.setTag(MOVE_PREV, new MovePrev(newPos.x(), newPos.y(), newPos.z(), nowOnGround));
        if (prev == null) return;

        // the client's last reported motion; a per-tick getPosition() snapshot would race the hit
        p.setTag(MOVE_VELOCITY, new Vec(newPos.x() - prev.x(), newPos.y() - prev.y(), newPos.z() - prev.z()));

        boolean wasOnGround = prev.onGround();
        double dy = newPos.y() - prev.y();

        // primary landing anchor; tick() only trails this
        if (nowOnGround || p.isFlying()) {
            if (nowOnGround) freezeOnLanding(p, now);
            p.removeTag(AIR_START_TICK);
            p.removeTag(LAUNCHED);
            return;
        }

        if (wasOnGround) {
            // rising packet = the jump tick, else a walk-off. vanilla seeds 0.42 even into a fluid, so latch unless
            // this is already a delivered-KB launch whose seed must survive the rise
            if (dy > 0 && !Boolean.TRUE.equals(p.getTag(LAUNCHED))) latchLaunch(p, now, newPos.yaw());
            p.setTag(AIR_START_TICK, now);
        } else {
            // mid-air re-launch, but not in a fluid/ladder
            if (p.getTag(AIR_START_TICK) == null) p.setTag(AIR_START_TICK, now);
            if (dy > 0 && p.getTag(ENV) == null && !Boolean.TRUE.equals(p.getTag(LAUNCHED))) latchLaunch(p, now, newPos.yaw());
        }
    }

    private static void latchLaunch(Player p, long now, double yaw) {
        boolean sprinting = p.isSprinting();
        Vec residual = residualAt(p, p.getTag(MOT_H), now);
        Vec boost = sprinting ? sprintJumpImpulse(yaw) : Vec.ZERO;
        Vec seedH = residual.add(boost);
        p.setTag(MOT_H, new MotState(seedH, now, true));
        p.setTag(LAUNCHED, true);
        p.setTag(LAUNCH_STAMP, new LaunchStamp(now, yaw, sprinting, residual, seedH));
        // a rising departure seeds motY (jumps + ground KB alike), like bF() firing after m()
        VertSim sim = p.getTag(VERT_SIM);
        if (sim != null) {
            double seedY = jumpSeed(VelocityConfig.JUMP_VELOCITY, p.getEffectLevel(PotionEffect.JUMP_BOOST));
            sim.clamped[0] = seedY;
            sim.raw[0] = seedY;
            sim.collided = false;
        }
    }

    /**
     * Vanilla jump takeoff motY. Both 1.8 ({@code bF()}) and 26 ({@code getJumpPower}) add Jump Boost as a separate
     * float term off the live effect, never an attribute, so it lives here like Levitation.
     * {@code jumpBoostAmplifier} is {@link net.minestom.server.entity.Entity#getEffectLevel} ({@code -1} = no effect).
     */
    static double jumpSeed(double base, int jumpBoostAmplifier) {
        if (jumpBoostAmplifier < 0) return base;
        return base + (double) ((float) (jumpBoostAmplifier + 1) * 0.1f);
    }

    /** Vanilla {@code bF()} sprint-jump horizontal impulse for a facing yaw (b/t). */
    private static Vec sprintJumpImpulse(double yaw) {
        return Directions.fromYaw(yaw).mul(SPRINT_IMPULSE);
    }

    /** The anchored residual bled to {@code now}, then the near-zero clamp so a stale residual snaps to 0. */
    private static Vec residualAt(Player p, MotState s, long now) {
        if (s == null) return Vec.ZERO;
        int ticks = (int) Math.max(0, now - s.sinceTick());
        Vec decayed = s.motH().mul(Math.pow(TickScaler.dragPerTick(p, frictionPerTick(p, s.airborne())), ticks));
        return new Vec(clampNearZero(decayed.x()), 0, clampNearZero(decayed.z()));
    }

    /** Both the {@code MOT_H} and entity-push residuals bleed by this. */
    static double frictionPerTick(Player p, boolean airborne) {
        // in a fluid the residual bleeds by the fluid's fixed friction; re-anchored on every env change so this holds
        // over the interval
        Env env = p.getTag(ENV);
        if (env == Env.WATER || env == Env.BUBBLE) { // bubble = water
            FluidFlow.Model m = p.getTag(FLOW_MODEL);
            return (m != null ? m : FluidFlow.Model.LEGACY).waterFriction(p.isSprinting());
        }
        if (env == Env.LAVA) return LAVA_FRICTION;
        if (env == Env.WEB) return 0.0;
        double hDrag = p.getAerodynamics().horizontalAirResistance();
        return (airborne ? hDrag : blockFriction(p) * hDrag) * blockSpeedFactor(p);
    }

    /** Vanilla {@code getBlockSpeedFactor}; soul sand also slows in 1.8 (value approximated). */
    private static double blockSpeedFactor(Player p) {
        Instance inst = p.getInstance();
        if (inst == null) return 1.0;
        Block below = blockBelow(p, p.getPosition());
        return below.compare(Block.SOUL_SAND) || below.compare(Block.HONEY_BLOCK) ? BLOCK_SPEED_FACTOR : 1.0;
    }

    /** Vanilla {@code frictionFactor}, what {@code PhysicsUtils} reads. */
    private static double blockFriction(Player p) {
        var instance = p.getInstance();
        if (instance == null) return DEFAULT_BLOCK_FRICTION;
        return blockBelow(p, p.getPosition()).registry().friction();
    }

    /** The supporting block under {@code pos}. */
    private static Block blockBelow(Player p, Point pos) {
        return MechanicsWorld.viewed(p).getBlock(pos.sub(0, 0.5000001, 0));
    }

    /** No-op if already grounded: re-anchors the residual so the gap to the next jump bleeds by ground friction. */
    private static void freezeOnLanding(Player p, long now) {
        MotState s = p.getTag(MOT_H);
        if (s == null || !s.airborne()) return;
        p.setTag(MOT_H, new MotState(residualAt(p, s, now), now, false));
    }

    private void tick(TickContext ctx) {
        for (Player p : ctx.world().players()) {
            if (!ctx.owns(p)) continue;
            long now = TickSystem.tick(p);
            // fallback, a tick behind onMove: catches status-only onGround packets (no PlayerMoveEvent)
            if (p.isOnGround()) {
                freezeOnLanding(p, now);
                p.removeTag(AIR_START_TICK);
                p.removeTag(LAUNCHED);
            }
            // detect the env before the sim so friction/gravity match
            VelocityRule rule = profiles.resolve(p, MechanicsKeys.VELOCITY);
            ClimbModel climbModel = VelocityRule.climbModel(rule);
            boolean modernBlocks = VelocityRule.modernBlockPhysicsEnabled(rule);
            FluidFlow.Model flowModel = VelocityRule.flowModel(rule);
            p.setTag(FLOW_MODEL, flowModel);
            Env env = tickEnvironment(p, now, environmentOf(p,
                    VelocityRule.fluidPhysicsEnabled(rule),
                    VelocityRule.climbPhysicsEnabled(rule),
                    VelocityRule.webPhysicsEnabled(rule),
                    climbModel, modernBlocks));
            // vanilla order: clamp, move, gravity, then entity push
            // a move processed during the previous dispatch stamps now-1 (this runs PRE_DISPATCH)
            Long lastMove = p.getTag(LAST_MOVE_TICK);
            boolean hold = VelocityRule.motYOnMovePacketEnabled(rule) && (lastMove == null || lastMove < now - 1);
            tickVertSim(p, env, climbModel, modernBlocks, flowModel, hold);
            // web zeroes everything, push included
            if (env == Env.WEB || !VelocityRule.entityPushEnabled(rule)) p.removeTag(ENTITY_PUSH);
            else tickEntityPush(p);
            // ticked even out of water so the residual bleeds out
            if (VelocityRule.flowPushEnabled(rule))
                tickFlowPush(p, env, flowModel, VelocityRule.flowLavaEnabled(rule));
            else p.removeTag(FLOW_PUSH);
        }
    }

    /** One vanilla {@code m()} travel step for the ticked motY: clamp, {@code move()} collide-zero, gravity - or the
     *  fluid / web / ladder vertical law when {@code env} is not {@link Env#NORMAL}. {@code hold} freezes the step. */
    private static void tickVertSim(Player p, Env env, ClimbModel climbModel, boolean modernBlocks, FluidFlow.Model flowModel, boolean hold) {
        VertSim sim = p.getTag(VERT_SIM);
        if (sim == null) p.setTag(VERT_SIM, sim = new VertSim());
        if (hold) return;
        if (p.isFlying() || p.getInstance() == null) {
            shift(sim.clamped, 0);
            shift(sim.raw, 0);
            sim.collided = p.isOnGround();
            return;
        }
        Aerodynamics aero = p.getAerodynamics();
        double g = aero.gravity(), s = aero.verticalAirResistance();
        double c = sim.clamped[0], r = sim.raw[0];

        if (env == Env.WEB) { shift(sim.clamped, 0); shift(sim.raw, 0); sim.collided = false; return; }

        // vanilla order: per-env start-of-tick adjust, then move() collides, then the per-env step
        if (env == Env.NORMAL && Math.abs(c) < VelocityConfig.CLAMP) c = 0;
        if (env == Env.LADDER) {
            c = Math.max(-LADDER_CLAMP, c); r = Math.max(-LADDER_CLAMP, r);
            if (p.isSneaking()) { if (c < 0) c = 0; if (r < 0) r = 0; } // sneak-hold
        }
        // the client's grounded flag lands a descent probe-free: the swept probe misses from flush-on-floor
        // positions, and without the flag a move-streaming grounded client sawtooths into false free-fall
        boolean grounded = p.isOnGround();
        PhysicsResult colC = c < 0 && !grounded ? CollisionUtils.handlePhysics(p, new Vec(0, c, 0)) : null;
        boolean collidedC = c < 0 && grounded || colC != null && colC.isOnGround();
        PhysicsResult colR = r < 0 && !grounded ? (r == c ? colC : CollisionUtils.handlePhysics(p, new Vec(0, r, 0))) : null;
        boolean collidedR = r < 0 && grounded || colR != null && colR.isOnGround();
        if (collidedC) c = landMotY(p, colC != null ? colC.newPosition() : p.getPosition(), c, modernBlocks);
        if (collidedR) r = landMotY(p, colR != null ? colR.newPosition() : p.getPosition(), r, modernBlocks);
        // climb-up: positionDelta.y is the ascent signal only, the model sets the value
        if (env == Env.LADDER) {
            OptionalDouble climbUp = climbModel.climbUpMotY(positionDelta(p).y());
            if (climbUp.isPresent()) { c = climbUp.getAsDouble(); r = climbUp.getAsDouble(); }
        }
        switch (env) {
            case WATER -> { double wg = TickScaler.gravityPerTick(p, flowModel.waterGravity(p.isSprinting())); double k = fluidSwim(p); double bump = TickScaler.impulse(p, LIQUID_EDGE_BUMP); double dr = TickScaler.dragPerTick(p, WATER_VERTICAL); boolean eb = edgeBump(p); shift(sim.clamped, eb ? bump : c * dr - wg + k); shift(sim.raw, eb ? bump : r * dr - wg + k); }
            case LAVA  -> { double k = fluidSwim(p); double bump = TickScaler.impulse(p, LIQUID_EDGE_BUMP); double dr = TickScaler.dragPerTick(p, LAVA_VERTICAL); double fg = TickScaler.gravityPerTick(p, FLUID_GRAVITY); boolean eb = edgeBump(p); shift(sim.clamped, eb ? bump : c * dr - fg + k); shift(sim.raw, eb ? bump : r * dr - fg + k); }
            // water drag, then the column drag toward its cap
            case BUBBLE -> { double wg = TickScaler.gravityPerTick(p, flowModel.waterGravity(p.isSprinting())); double dr = TickScaler.dragPerTick(p, WATER_VERTICAL); boolean dn = bubbleDown(p); shift(sim.clamped, bubbleStep(p, c * dr - wg, dn)); shift(sim.raw, bubbleStep(p, r * dr - wg, dn)); }
            // floor a descent at the slide speed
            case HONEY -> { double slide = TickScaler.impulse(p, HONEY_SLIDE); shift(sim.clamped, Math.max(airStep(p, c, g, s), slide)); shift(sim.raw, Math.max(airStep(p, r, g, s), slide)); }
            default    -> { shift(sim.clamped, airStep(p, c, g, s)); shift(sim.raw, airStep(p, r, g, s)); } // normal + ladder
        }
        sim.collided = collidedC;
    }

    /** Vanilla swim input in a fluid: jump rises only while airborne. */
    private static double fluidSwim(Player p) {
        PlayerInputs in = p.inputs();
        double up = !p.isOnGround() && in.jump() ? FLUID_SWIM_STEP : 0;
        double down = in.shift() ? FLUID_SWIM_STEP : 0;
        return TickScaler.impulse(p, up - down);
    }

    /**
     * Vanilla water/lava edge-bump (26 {@code jumpOutOfFluid}). A HEURISTIC reconstructing the
     * {@code horizontalCollision} Minestom doesn't track: movement key held + ~0 horizontal displacement + headroom.
     */
    private static boolean edgeBump(Player p) {
        PlayerInputs in = p.inputs();
        if (!(in.forward() || in.backward() || in.left() || in.right())) return false;
        Vec d = positionDelta(p);
        if (d.x() * d.x() + d.z() * d.z() > 0.0004) return false; // displaced horizontally -> not blocked
        Instance inst = p.getInstance();
        return inst != null && !CollisionUtils.handlePhysics(p, new Vec(0, 0.6, 0)).collisionY();
    }

    /** 26 {@code Entity.handleOnInsideBubbleColumn}: motY drags toward the column cap. */
    private static double bubbleStep(Player p, double v, boolean down) {
        return down ? Math.max(TickScaler.impulse(p, BUBBLE_DOWN_CAP), v - TickScaler.impulse(p, BUBBLE_DOWN_STEP))
                : Math.min(TickScaler.impulse(p, BUBBLE_UP_CAP), v + TickScaler.impulse(p, BUBBLE_UP_STEP));
    }

    /** Magma base ({@code drag=true}) drags down, soul-sand base up. */
    private static boolean bubbleDown(Player p) {
        Instance inst = p.getInstance();
        return inst != null && "true".equals(MechanicsWorld.viewed(p).getBlock(p.getPosition()).getProperty("drag"));
    }

    /**
     * Post-landing motY for a downward collision: vanilla zeroes it, but a bounce block inverts it ({@code bounceUp})
     * unless the player sneaks - slime at factor {@code 1.0}, and with {@code modernBlocks} a bed at {@code 0.66}
     * (26-only, {@code BedBlock:150}; 1.8 beds do not bounce).
     */
    private static double landMotY(Player p, Point landedAt, double motY, boolean modernBlocks) {
        if (!p.isSneaking()) {
            Instance inst = p.getInstance();
            if (inst != null) {
                Block below = blockBelow(p, landedAt);
                if (below.compare(Block.SLIME_BLOCK)) return -motY;
                if (modernBlocks && isBed(below)) return -motY * 0.66;
            }
        }
        return 0;
    }

    /** Any colour, via the {@code #minecraft:beds} tag with a {@code _bed} key-suffix fallback. */
    private static boolean isBed(Block b) {
        return BEDS != null ? BEDS.contains(b) : b.key().value().endsWith("_bed");
    }

    /**
     * One NORMAL/LADDER vertical air step: gravity then drag, with Levitation and Slow Falling (both 26-only) when the
     * effect is present.
     */
    private static double airStep(Player p, double v, double g, double s) {
        double drag = TickScaler.dragPerTick(p, s);
        int lev = p.getEffectLevel(PotionEffect.LEVITATION);
        if (lev >= 0) return (v + (LEVITATION_BASE * (lev + 1) - v) * LEVITATION_RATE) * drag;
        double gEff = v <= 0 && p.hasEffect(PotionEffect.SLOW_FALLING) ? Math.min(g, SLOW_FALL_GRAVITY) : g;
        return (v - TickScaler.gravityPerTick(p, gEff)) * drag;
    }

    /** Re-anchors the residual at an {@link Env} boundary so each segment bleeds by its own friction. */
    private static Env tickEnvironment(Player p, long now, Env env) {
        Env old = envOf(p);
        if (env != old) {
            MotState s = p.getTag(MOT_H);
            if (s != null) {
                Vec frozen = residualAt(p, s, now); // bled by the OLD env friction - the ENV tag is still 'old' here
                Vec adjusted = switch (env) {
                    case WEB -> Vec.ZERO;
                    case LADDER -> new Vec(clampAbs(frozen.x(), LADDER_CLAMP), 0, clampAbs(frozen.z(), LADDER_CLAMP));
                    default -> frozen;
                };
                if (adjusted.isZero()) p.removeTag(MOT_H);
                else p.setTag(MOT_H, new MotState(adjusted, now, s.airborne()));
            }
            if (env == Env.NORMAL) p.removeTag(ENV);
            else p.setTag(ENV, env);
        }
        return env;
    }

    private static Env envOf(Player p) {
        Env e = p.getTag(ENV);
        return e == null ? Env.NORMAL : e;
    }

    /** Vanilla precedence WEB &gt; WATER &gt; LAVA &gt; LADDER &gt; HONEY; a disabled medium falls through. */
    private static Env environmentOf(Player p, boolean fluidOn, boolean climbOn, boolean webOn, ClimbModel climbModel, boolean modernBlocks) {
        if (p.getInstance() == null) return Env.NORMAL;
        MechanicsWorld inst = MechanicsWorld.viewed(p);
        if (webOn && BlockContact.touching(p, modernBlocks ? IS_WEB_MODERN : IS_WEB)) return Env.WEB;
        BoundingBox box = p.getBoundingBox();
        Pos pos = p.getPosition();
        // a water variant, checked before water
        if (fluidOn && modernBlocks && inst.getBlock(pos).compare(Block.BUBBLE_COLUMN)) return Env.BUBBLE;
        if (fluidOn && BlockContact.scanCells(inst, pos, BlockContact.adjust(box, 0, -0.4, 0), BlockContact.VANILLA_INSET, IS_WATER))
            return Env.WATER;
        if (fluidOn && BlockContact.scanCells(inst, pos, BlockContact.adjust(box, -0.1, -0.4, -0.1), 0, IS_LAVA))
            return Env.LAVA;
        if (!climbOn) return Env.NORMAL;
        if (p.getGameMode() != GameMode.SPECTATOR && climbModel.isClimbable(inst.getBlock(pos))) {
            return Env.LADDER;
        }
        if (modernBlocks && BlockContact.touching(p, IS_HONEY)) return Env.HONEY;
        return Env.NORMAL;
    }

    /** Vanilla {@code MathHelper.a}: clamps {@code v} to {@code [-limit, limit]}. */
    private static double clampAbs(double v, double limit) {
        return Math.max(-limit, Math.min(limit, v));
    }

    /** Vanilla {@code m()} near-zero clamp: zeroes one component whose magnitude is below {@link VelocityConfig#CLAMP}. */
    private static double clampNearZero(double v) {
        return Math.abs(v) < VelocityConfig.CLAMP ? 0.0 : v;
    }

    private static void shift(double[] h, double v) {
        for (int i = h.length - 1; i > 0; i--) h[i] = h[i - 1];
        h[0] = v;
    }

    /**
     * Replaces the tracked motion - residuals were consumed by the delivery's own fold. Measured on instrumented Paper
     * 1.8.8: the impulse stays in server mot, decays by travel friction (~7-15 ticks) and folds into hits in that
     * window (the rod-&gt;hit combo); only player-melee restores. LAUNCHED keeps the rising move packet from re-seeding
     * the jump value over the KB arc.
     */
    public static void foldDelivered(Entity entity, Vec bt) {
        if (!(entity instanceof Player p)) return;
        long now = TickSystem.tick(p);
        p.setTag(MOT_H, new MotState(new Vec(bt.x(), 0, bt.z()), now, !p.isOnGround()));
        p.removeTag(ENTITY_PUSH);
        p.removeTag(FLOW_PUSH);
        VertSim sim = p.getTag(VERT_SIM);
        if (sim == null) p.setTag(VERT_SIM, sim = new VertSim());
        sim.clamped[0] = bt.y();
        sim.raw[0] = bt.y();
        sim.collided = false;
        if (bt.y() > 0) p.setTag(LAUNCHED, true);
    }

    /** Vanilla collision {@code onGround}: fires before a laggy client's landing packet, so fall damage ends in sync with the combat ground checks. */
    public static boolean simCollided(Entity entity) {
        if (!(entity instanceof Player p)) return false;
        VertSim sim = p.getTag(VERT_SIM);
        return sim != null && sim.collided;
    }

    /** {@code lookback} reads that many end-of-tick values back; {@code clamped} picks the apex-reseed variant. */
    public static @Nullable Double serverMotY(Entity entity, int lookback, boolean clamped) {
        if (!(entity instanceof Player p)) return null;
        VertSim sim = p.getTag(VERT_SIM);
        if (sim == null) return null;
        int i = Math.min(Math.max(lookback, 0), VertSim.HISTORY - 1);
        return clamped ? sim.clamped[i] : sim.raw[i];
    }

    // entity push residual

    /** Vanilla {@code bL()} list range: own bounding box grown {@code 0.2} per side (expand takes total size). */
    private static final double PUSH_GROW = 0.4;
    /** Vanilla {@code Entity.collide} impulse. */
    private static final double PUSH_IMPULSE = 0.05;
    /** Vanilla skips the push below this absMax horizontal distance, so perfectly stacked players never separate. */
    private static final double PUSH_MIN_DIST = 0.01;
    /** Generous enough for any overlapping entity's half-width. */
    private static final double PUSH_QUERY_RANGE = 3.0;

    /** Vanilla per-tick order: travel bleeds the existing residual, then {@code bL()} adds this tick's pushes raw. */
    private static void tickEntityPush(Player p) {
        Vec acc = bleed(p, entityPush(p)).add(pushesFor(p));
        if (acc.isZero()) p.removeTag(ENTITY_PUSH);
        else p.setTag(ENTITY_PUSH, acc);
    }

    /** One friction step + vanilla's near-zero clamp, mirroring the travel step the residual rode in vanilla. */
    private static Vec bleed(Player p, Vec acc) {
        if (acc.isZero()) return acc;
        Vec decayed = acc.mul(frictionPerTick(p, !p.isOnGround()));
        return new Vec(clampNearZero(decayed.x()), clampNearZero(decayed.y()), clampNearZero(decayed.z()));
    }

    /** Vanilla {@code Entity.collide} from every overlapping living entity. */
    private static Vec pushesFor(Player p) {
        var instance = p.getInstance();
        if (instance == null) return Vec.ZERO;
        var range = p.getBoundingBox().expand(PUSH_GROW, 0, PUSH_GROW);

        double px = 0, pz = 0;
        for (Entity other : MechanicsWorld.of(p).nearbyEntities(p.getPosition(), PUSH_QUERY_RANGE)) {
            if (other == p || !(other instanceof LivingEntity living) || living.isDead()) continue;
            if (other instanceof Player op && op.getGameMode() == GameMode.SPECTATOR) continue;
            if (!WorldPolicy.canAffect(other, p)) continue;
            if (!range.intersectEntity(p.getPosition(), other)) continue;

            double dx = other.getPosition().x() - p.getPosition().x();
            double dz = other.getPosition().z() - p.getPosition().z();
            // vanilla normalizes by sqrt(absMax), not euclidean
            double absMax = Math.max(Math.abs(dx), Math.abs(dz));
            if (absMax < PUSH_MIN_DIST) continue;
            double norm = Math.sqrt(absMax);
            double scale = Math.min(1.0, 1.0 / norm) / norm * PUSH_IMPULSE;

            // a non-player living pushes only every other tick
            int passes = other instanceof Player || other.getAliveTicks() % 2 != 0 ? 2 : 1;
            px -= dx * scale * passes;
            pz -= dz * scale * passes;
        }
        return new Vec(px, 0, pz);
    }

    /** Zero-Y, b/t. Folded by a {@link VelocityRule} arc when {@link VelocityConfig#entityPush()} is on. */
    public static Vec entityPush(Entity entity) {
        if (!(entity instanceof Player p)) return Vec.ZERO;
        Vec v = p.getTag(ENTITY_PUSH);
        return v == null ? Vec.ZERO : v;
    }

    // water flow residual

    /**
     * Add-then-bleed: vanilla adds the current in {@code K()} BEFORE the travel friction. A non-fluid env feeds a zero
     * impulse so the residual bleeds out after leaving the fluid.
     */
    private static void tickFlowPush(Player p, Env env, FluidFlow.Model model, boolean lavaEnabled) {
        Vec impulse;
        if (env == Env.WATER) {
            impulse = flowImpulse(p, model, Block.WATER, FLOW_IMPULSE);
        } else if (env == Env.LAVA && model.pushesInLava() && lavaEnabled) {
            impulse = flowImpulse(p, model, Block.LAVA, LAVA_FLOW_IMPULSE);
        } else {
            impulse = Vec.ZERO;
        }
        Vec acc = bleed(p, flowPush(p).add(impulse));
        if (model.zeroesAgainstWall()) acc = zeroBlockedAxes(p, acc);
        if (acc.isZero()) p.removeTag(FLOW_PUSH);
        else p.setTag(FLOW_PUSH, acc);
    }

    private static Vec flowImpulse(Player p, FluidFlow.Model model, Block fluid, double scale) {
        Instance instance = p.getInstance();
        if (instance == null) return Vec.ZERO;
        Vec mov = positionDelta(p);
        return model.impulse(MechanicsWorld.viewed(p), p.getPosition(), p.getBoundingBox(), fluid, scale, mov.x(), mov.z());
    }

    /**
     * Mirrors vanilla {@code Entity.move}'s {@code if (collided) motX/motZ = 0}, keeping y. Measured live (motlog): a
     * KB into a wall zeroes that mot axis on the next tick while the free axes decay, so wall-pinned residuals must
     * read 0. Probed at READ time (currently blocked) as a cheap stand-in for vanilla's per-tick zeroing.
     */
    public static Vec zeroBlockedAxes(Entity entity, Vec acc) {
        if (acc.isZero() || !(entity instanceof Player p) || p.getInstance() == null) return acc;
        PhysicsResult r = CollisionUtils.handlePhysics(p, new Vec(acc.x(), 0, acc.z()));
        return new Vec(r.collisionX() ? 0.0 : acc.x(), acc.y(), r.collisionZ() ? 0.0 : acc.z());
    }

    /** Players only. Folded by a {@link VelocityRule} arc (x/z current + the Y down-term) when {@link VelocityConfig#flowPush()} is on. */
    public static Vec flowPush(Entity entity) {
        if (!(entity instanceof Player p)) return Vec.ZERO;
        Vec v = p.getTag(FLOW_PUSH);
        return v == null ? Vec.ZERO : v;
    }

    /** Ticks since the client's jump packet, so ping-invariant. 0 on the ground / with no air anchor. */
    public static int ticksInAir(Entity entity) {
        if (entity == null) return 0;
        Long start = entity.getTag(AIR_START_TICK);
        if (start == null) return 0;
        return (int) Math.max(0, TickSystem.tick(entity) - start);
    }

    /** An upward-launched arc (jump/KB boost) vs a ledge walk-off. Cleared on landing or flight. */
    public static boolean launched(Entity entity) {
        return entity instanceof Player p && Boolean.TRUE.equals(p.getTag(LAUNCHED));
    }

    /** {@code null} outside a launch arc. */
    public static @Nullable JumpInfo recentJump(Entity entity) {
        if (!(entity instanceof Player p) || !launched(p)) return null;
        LaunchStamp s = p.getTag(LAUNCH_STAMP);
        return s == null ? null : new JumpInfo(s.yaw(), s.sprinting(), s.residualH(), s.seedH());
    }

    /**
     * Friction-bled to {@code now + tickOffset} - the value every hit folds. The victim's own simulated motion, not
     * prior knockback: vanilla restores the pre-KB velocity after broadcasting it.
     */
    public static Vec horizontalMot(Entity entity, int tickOffset) {
        if (!(entity instanceof Player p)) return Vec.ZERO;
        return residualAt(p, p.getTag(MOT_H), TickSystem.tick(p) + tickOffset);
    }

    /**
     * Vanilla's {@code motX/motZ *= 0.6} attacker self-slowdown on a landed sprint hit. Mutates only the tracked
     * velocity, not damage/KB.
     */
    public static void scaleHorizontalResidual(Entity entity, double factor) {
        if (!(entity instanceof Player p)) return;
        MotState s = p.getTag(MOT_H);
        if (s == null) return;
        long now = TickSystem.tick(p);
        Vec bled = residualAt(p, s, now);
        p.setTag(MOT_H, new MotState(bled.mul(factor), now, s.airborne()));
    }

    /** Move-delta velocity (b/t): players via the per-move snapshot, others via entity velocity. */
    public static Vec positionDelta(Entity entity) {
        if (entity instanceof Player p) {
            Vec d = p.getTag(MOVE_VELOCITY);
            return d == null ? Vec.ZERO : d;
        }
        // entity velocity is blocks/second
        return entity.getVelocity().div(ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    public static boolean onGround(@Nullable Entity entity) {
        return onGround(entity, DEFAULT_GROUND_TICKS);
    }

    /**
     * Whether the entity is on the ground (vanilla {@code verticalCollision && motY < 0}): {@code 0} = the raw client
     * flag; {@code >= 1} = the flag OR a collision probe seeded from the server-simulated {@code motY} (so a high-ping
     * victim grounds about as fast as vanilla), forward-predicting {@code ticks} deep. A rising arc is never grounded.
     * Runs a collision sweep - query per hit/event, not per tick.
     */
    public static boolean onGround(@Nullable Entity entity, int ticks) {
        if (entity == null) return false;
        if (ticks <= 0 || !(entity instanceof Player p)) return entity.isOnGround();
        if (entity.isOnGround()) return true;
        if (entity.getInstance() == null) return false;

        if (simCollided(p)) return true;

        Aerodynamics aero = entity.getAerodynamics();
        double g = aero.gravity();
        double s = aero.verticalAirResistance();
        double vy = serverSimMotY(p, g, s); // server arc, not the client delta
        if (vy >= 0) return false;
        double probe = 0;
        for (int i = 0; i < ticks; i++) {
            probe += vy;
            vy = (vy - g) * s;
        }
        PhysicsResult r = CollisionUtils.handlePhysics(entity, new Vec(0, probe, 0));
        return r.isOnGround();
    }

    /** The ticked sim's latest value, else the analytic gravity arc. Unlike {@link #positionDelta} it keeps accelerating, so a high-ping fall is probed at the depth vanilla sees. */
    private static double serverSimMotY(Player p, double g, double s) {
        VertSim sim = p.getTag(VERT_SIM);
        if (sim != null) return sim.clamped[0];
        int air = ticksInAir(p);
        double vy = launched(p) ? VelocityConfig.JUMP_VELOCITY : 0.0;
        // air is in server ticks, so step with TPS-scaled gravity/drag to match the sim
        double gp = TickScaler.gravityPerTick(p, g), sp = TickScaler.dragPerTick(p, s);
        for (int i = 0; i < air; i++) vy = (vy - gp) * sp;
        return vy;
    }

    /** Airborne and descending; a fluid/web/ladder is not free fall. */
    public static boolean isFalling(@Nullable Entity entity) {
        if (entity == null) return false;
        if (entity instanceof Player p) {
            if (p.isFlying()) return false;
            if (p.getTag(ENV) != null) return false;
        }
        if (onGround(entity)) return false;
        return positionDelta(entity).y() < 0;
    }
}
