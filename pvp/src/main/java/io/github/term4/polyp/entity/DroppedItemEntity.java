package io.github.term4.polyp.entity;

import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.world.ExternallyTickable;
import io.github.term4.polyp.vri.ItemSpawnEvent;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.tracking.motion.FluidFlow;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityItemMergeEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link ItemEntity} with the vanilla environment response Minestom omits: fluid current/buoyancy, lava pop,
 * push-out-of-blocks. {@link Model#LEGACY} = 1.8 (items sink, the current slides them along the bottom);
 * {@link Model#MODERN} = 26.1 (items float). Stock physics stay in charge; {@link #update} nudges velocity
 * post-move, where both vanillas apply their fluid steps.
 */
public class DroppedItemEntity extends ItemEntity implements ExternallyTickable {

    // TODO: Simplify, a lot of this has already been handled either in minestom, or in movement tracker.
    public static final Key KEY = Key.key("polyp:item");

    // @ApiStatus.Internal override: super is exactly this field write + dispatcher().updateElement (verified
    // 2026.07.12-26.2, re-verify on bumps) - an externally ticked entity in the global dispatcher double-ticks
    @Override protected void refreshCurrentChunk(@NotNull net.minestom.server.instance.Chunk chunk) {
        if (MechanicsWorld.externallyTicked(this)) {
            currentChunk = chunk;
            return;
        }
        super.refreshCurrentChunk(chunk);
    }

    @Override public void tick(long time) {
        if (!MechanicsWorld.ownsCurrentTick(this)) return;
        super.tick(time);
    }

    public enum Model { LEGACY, MODERN }

    private static final double WATER_DRAG_MODERN = 0.99;
    private static final double LAVA_DRAG_MODERN = 0.95;
    private static final double BUOYANCY = 5.0E-4;         // 26.1: +lift while vy < 0.06
    private static final double BUOYANCY_CAP = 0.06;
    private static final double FLOAT_HEIGHT = 0.1;        // 26.1: fluid height gating float vs gravity
    private static final double FLOW_SCALE = 0.014;
    private static final double LAVA_FLOW_SCALE = 0.0023333333333333335; // 26.1 overworld (nether FAST_LAVA: 0.007)
    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;

    private final @Nullable Model model;

    private PhysicsResult lastPhysics;
    private long lastMergeCheck;
    private boolean landedLastTick;
    private @Nullable Model resolved;
    // lava-pop cadence: vanilla pops on int-cast cell change or every 25 ticks
    private int cellX = Integer.MIN_VALUE, cellY, cellZ;
    // last tick's sample chose fluid motion - vanilla skips gravity that tick; Minestom applied it anyway
    private boolean fluidMotion;

    /** {@code model} null = resolve from the profile ({@code MechanicsKeys.ITEM_PHYSICS}), falling back to LEGACY. */
    public DroppedItemEntity(@NotNull ItemStack itemStack, @Nullable Model model) {
        super(itemStack);
        this.model = model;
        // world fork/respawn cloners read this stamp
        setTag(MechanicsWorld.ENTITY_COPY, () -> {
            DroppedItemEntity copy = new DroppedItemEntity(getItemStack(), this.model);
            copy.setVelocity(getVelocity());
            return copy;
        });
        // world saves persist this descriptor; apps revive it via fromSave
        setTag(MechanicsWorld.ENTITY_SAVE, () -> {
            Vec vel = getVelocity();
            CompoundBinaryTag.Builder out = CompoundBinaryTag.builder().putString("id", "polyp:item")
                    .put("item", ItemStack.CODEC.encode(Transcoder.NBT, getItemStack()).orElseThrow())
                    .put("vel", ListBinaryTag.builder(BinaryTagTypes.DOUBLE)
                            .add(DoubleBinaryTag.doubleBinaryTag(vel.x()))
                            .add(DoubleBinaryTag.doubleBinaryTag(vel.y()))
                            .add(DoubleBinaryTag.doubleBinaryTag(vel.z())).build());
            if (this.model != null) out.putString("model", this.model.name());
            return out.build();
        });
    }

    /** The load-side reviver for {@code "polyp:item"} {@link MechanicsWorld#ENTITY_SAVE} descriptors. */
    public static @NotNull DroppedItemEntity fromSave(@NotNull CompoundBinaryTag data) {
        String model = data.getString("model");
        DroppedItemEntity item = new DroppedItemEntity(
                ItemStack.CODEC.decode(Transcoder.NBT, data.get("item")).orElseThrow(),
                model.isEmpty() ? null : Model.valueOf(model));
        ListBinaryTag vel = data.getList("vel", BinaryTagTypes.DOUBLE);
        if (vel.size() == 3) item.setVelocity(new Vec(vel.getDouble(0), vel.getDouble(1), vel.getDouble(2)));
        return item;
    }

    /** Spawns a player-attributed drop ({@code velocity} in b/t) into the player's world; {@code null} if cancelled. */
    public static @Nullable DroppedItemEntity spawn(@NotNull Instance instance, @NotNull Pos pos, @NotNull Vec velocity,
                                                    @NotNull ItemStack stack, @Nullable Model physics, int pickupDelayTicks,
                                                    ItemSpawnEvent.@NotNull Cause cause, @NotNull Player player) {
        return spawn(MechanicsWorld.of(player), pos, velocity, stack, physics, pickupDelayTicks, cause, player);
    }

    /** Server/minigame drop straight into {@code world} (generators, kill drops); {@code null} if cancelled. */
    public static @Nullable DroppedItemEntity spawn(@NotNull MechanicsWorld world, @NotNull Pos pos, @NotNull Vec velocity,
                                                    @NotNull ItemStack stack, @Nullable Model physics, int pickupDelayTicks) {
        return spawn(world, pos, velocity, stack, physics, pickupDelayTicks, ItemSpawnEvent.Cause.SERVER, null);
    }

    private static @Nullable DroppedItemEntity spawn(MechanicsWorld world, Pos pos, Vec velocity, ItemStack stack,
                                                     @Nullable Model physics, int pickupDelayTicks,
                                                     ItemSpawnEvent.Cause cause, @Nullable Player player) {
        DroppedItemEntity item = new DroppedItemEntity(stack, physics);
        // armed BEFORE the async world spawn, so an instance-less item resolves no scope - use the dropper's
        Entity scope = player != null ? player : item;
        item.setPickupDelay(TickScaler.duration(scope, pickupDelayTicks, KEY), TimeUnit.SERVER_TICK);
        item.setVelocity(TickScaler.fromClientVelocity(scope, velocity).mul(TPS));
        ItemSpawnEvent event = new ItemSpawnEvent(item, cause, player, world, pos);
        EventDispatcher.call(event);
        if (event.isCancelled()) return null;
        world.spawn(item, pos);
        return item;
    }

    @Override
    public void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle == null && getInstance() != null) {
            this.lastPhysics = MechanicsWorld.step(this, velocity.div(TPS), lastPhysics, result -> {
                this.velocity = result.newVelocity().mul(TPS);
                this.onGround = result.isOnGround();
                refreshPosition(result.newPosition(), true, false); // ITEM is a synchronize-only type
            });
        }
        // ItemEntity's landing sync, once on first touchdown
        if (!landedLastTick && onGround) {
            synchronizePosition();
            sendPacketToViewers(new net.minestom.server.network.packet.server.play.EntityVelocityPacket(
                    getEntityId(), TickScaler.wireVelocity(this, getVelocity().div(TPS))));
        }
        landedLastTick = onGround;
    }

    @Override
    public void update(long time) {
        Instance instance = getInstance();
        if (instance == null || isRemoved()) return;
        mergeScan(time);
        Pos pos = getPosition();
        Vec v0 = getVelocity().div(TPS); // b/t; Minestom already applied gravity + drag this tick

        MechanicsWorld world = MechanicsWorld.of(this);
        Vec v = effectiveModel() == Model.LEGACY ? legacyTick(world, pos, v0) : modernTick(world, pos, v0);
        if (buried(world, pos)) v = pushOutOfBlocks(world, pos, v);

        // silent: setVelocity broadcasts, fighting the client's own item sim
        if (!v.samePoint(v0)) this.velocity = v.mul(TPS);
    }

    // super.update's scan, minus its isPickable() gates: vanilla merges ignore the pickup delay
    // (1.8 EntityItem.combineItems excludes only the infinite 32767) - fresh drops merge immediately
    private void mergeScan(long time) {
        Duration delay = getMergeDelay();
        if (!isMergeable() || (delay != null && Cooldown.hasCooldown(time, lastMergeCheck, delay))) return;
        this.lastMergeCheck = time;
        instance.getEntityTracker().nearbyEntities(position, getMergeRange(), EntityTracker.Target.ITEMS, other -> {
            if (other == this || !other.isMergeable()) return;
            ItemStack stack = getItemStack();
            ItemStack otherStack = other.getItemStack();
            if (!stack.isSimilar(otherStack)) return;
            int total = stack.amount() + otherStack.amount();
            if (!MathUtils.isBetween(total, 0, stack.maxStackSize())) return;
            EntityItemMergeEvent merge = new EntityItemMergeEvent(this, other, stack.withAmount(total));
            EventDispatcher.callCancellable(merge, () -> {
                setItemStack(merge.getResult());
                other.remove();
            });
        });
    }

    private Vec legacyTick(MechanicsWorld world, Pos pos, Vec v) {
        int bx = (int) pos.x(), by = (int) pos.y(), bz = (int) pos.z();
        boolean cellChanged = bx != cellX || by != cellY || bz != cellZ;
        cellX = bx;
        cellY = by;
        cellZ = bz;
        if ((cellChanged || getAliveTicks() % 25 == 0)
                && world.getBlock(pos, Block.Getter.Condition.TYPE).compare(Block.LAVA)) {
            var rnd = ThreadLocalRandom.current();
            v = new Vec((rnd.nextFloat() - rnd.nextFloat()) * 0.2, 0.2, (rnd.nextFloat() - rnd.nextFloat()) * 0.2);
            world.playSound(Sound.sound(SoundEvent.BLOCK_FIRE_EXTINGUISH.key(), Sound.Source.NEUTRAL,
                    0.4f, 2.0f + rnd.nextFloat() * 0.4f), pos);
        }
        // twice per tick (Entity.onEntityUpdate + the EntityItem tail); half-speed sliding rubber-bands
        // against the client's own sim
        Vec flow = FluidFlow.itemLegacyFlow(world, pos, getBoundingBox());
        return flow.isZero() ? v : v.add(flow.mul(TickScaler.impulse(this, 2 * FLOW_SCALE)));
    }

    private Vec modernTick(MechanicsWorld world, Pos pos, Vec v) {
        var water = FluidFlow.itemModernSample(world, pos, getBoundingBox(), Block.WATER);
        var lava = water.height() > 0 ? FluidFlow.ItemSample.EMPTY
                : FluidFlow.itemModernSample(world, pos, getBoundingBox(), Block.LAVA);

        // vanilla skipped gravity this tick - invert exactly what Minestom's aerodynamics (registry item 0.04/0.98) applied
        if (fluidMotion) {
            Aerodynamics aero = TickScaler.aerodynamics(this, getAerodynamics());
            v = v.withY(v.y() + aero.gravity() * aero.verticalAirResistance());
        }
        // twice per tick (baseTick + the item tail); sequential - the near-still floor reads v between pushes
        for (int push = 0; push < 2; push++) {
            if (water.height() > 0) v = v.add(water.current(TickScaler.impulse(this, FLOW_SCALE), v.x(), v.z()));
            else if (lava.height() > 0) v = v.add(lava.current(TickScaler.impulse(this, LAVA_FLOW_SCALE), v.x(), v.z()));
        }

        fluidMotion = water.height() > FLOAT_HEIGHT || lava.height() > FLOAT_HEIGHT;
        if (fluidMotion) {
            double drag = TickScaler.dragPerTick(this, water.height() > FLOAT_HEIGHT ? WATER_DRAG_MODERN : LAVA_DRAG_MODERN);
            double vy = v.y() + (v.y() < TickScaler.impulse(this, BUOYANCY_CAP) ? TickScaler.impulse(this, BUOYANCY) : 0.0);
            v = new Vec(v.x() * drag, vy, v.z() * drag);
        }
        return v;
    }

    private boolean buried(MechanicsWorld world, Pos pos) {
        return world.getBlock((int) Math.floor(pos.x()), (int) Math.floor(centerY(pos)), (int) Math.floor(pos.z()),
                Block.Getter.Condition.TYPE).isSolid();
    }

    private double centerY(Pos pos) {
        return pos.y() + getBoundingBox().height() / 2.0;
    }

    private Model effectiveModel() {
        if (model != null) return model;
        if (resolved == null && Polyp.getInstance().isInitialized()) {
            Model r = Polyp.getInstance().profiles().resolve(this, MechanicsKeys.ITEM_PHYSICS);
            resolved = r != null ? r : Model.LEGACY; // once; profile swaps don't retarget live items
        }
        return resolved != null ? resolved : Model.LEGACY;
    }

    /** Vanilla {@code Entity.pushOutOfBlocks}: inside a solid block, shove {@code 0.1..0.3} toward the nearest free face (up wins ties). */
    private Vec pushOutOfBlocks(MechanicsWorld world, Pos pos, Vec v) {
        double cy = centerY(pos);
        int bx = (int) Math.floor(pos.x()), by = (int) Math.floor(cy), bz = (int) Math.floor(pos.z());

        double dx = pos.x() - bx, dy = cy - by, dz = pos.z() - bz;
        double best = 9999.0;
        int dir = 3; // up default, like vanilla
        if (free(world, bx - 1, by, bz) && dx < best) { best = dx; dir = 0; }
        if (free(world, bx + 1, by, bz) && 1.0 - dx < best) { best = 1.0 - dx; dir = 1; }
        if (free(world, bx, by + 1, bz) && 1.0 - dy < best) { best = 1.0 - dy; dir = 3; }
        if (free(world, bx, by, bz - 1) && dz < best) { best = dz; dir = 4; }
        if (free(world, bx, by, bz + 1) && 1.0 - dz < best) { dir = 5; }
        double f = ThreadLocalRandom.current().nextFloat() * 0.2 + 0.1;
        return switch (dir) {
            case 0 -> v.withX(-f);
            case 1 -> v.withX(f);
            case 4 -> v.withZ(-f);
            case 5 -> v.withZ(f);
            default -> v.withY(f);
        };
    }

    private static boolean free(MechanicsWorld world, int x, int y, int z) {
        return !world.getBlock(x, y, z, Block.Getter.Condition.TYPE).isSolid();
    }
}
