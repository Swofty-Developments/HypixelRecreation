package io.github.term4.polyp.tracking.motion;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla fluid <b>flow</b> direction - the current that shoves an entity downstream. Pure geometry over the world's
 * fluid cells; it moves nothing itself (the caller carries it as a friction-bled residual, like vanilla's
 * {@code motX/motY/motZ}). Per-cell slope mirrors 1.8 {@code BlockFluids.h} / 26 {@code FlowingFluid.getFlow};
 * {@link Model} selects how the summed cells become the push.
 */
public final class FluidFlow {

    /**
     * Pluggable fluid behavior: the current impulse plus the per-version water quirks. Built-ins {@link #LEGACY} (1.8,
     * flat {@code normalize(sum) x scale}) and {@link #MODERN} (26.1, averaged + depth-scaled), or a custom impl;
     * selected per preset via {@code VelocityConfig.flowModel}.
     */
    public interface Model {

        /** Already x {@code scale}. {@code movX/movZ} = horizontal velocity, read only by MODERN's near-still floor. */
        Vec impulse(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid, double scale, double movX, double movZ);

        /** motY (b/t) subtracted per WATER tick; lava gravity is model-agnostic. */
        double waterGravity(boolean sprinting);

        double waterFriction(boolean sprinting);

        /** 1.8 yes, 26 no: a current pinning a player against a wall never accumulates. */
        boolean zeroesAgainstWall();

        /** 1.8 no, 26 yes - still subject to the {@code flowLava} config gate. */
        boolean pushesInLava();

        /**
         * Modern horizontal current/friction with the 1.8 sink rate (Hypixel feel). Only {@link #waterGravity} changes;
         * {@link #impulse}, including its falling-water Y, stays this model's.
         */
        default Model withLegacyWaterGravity() {
            Model base = this;
            return new Model() {
                @Override public Vec impulse(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid, double scale, double movX, double movZ) {
                    return base.impulse(inst, pos, box, fluid, scale, movX, movZ);
                }
                @Override public double waterGravity(boolean sprinting) { return LEGACY.waterGravity(sprinting); }
                @Override public double waterFriction(boolean sprinting) { return base.waterFriction(sprinting); }
                @Override public boolean zeroesAgainstWall() { return base.zeroesAgainstWall(); }
                @Override public boolean pushesInLava() { return base.pushesInLava(); }
            };
        }

        Model LEGACY = new Model() {
            @Override public Vec impulse(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid, double scale, double movX, double movZ) {
                Vec dir = legacyFlow(inst, pos, box, fluid);
                return dir.isZero() ? Vec.ZERO : dir.mul(scale);
            }
            @Override public double waterGravity(boolean sprinting) { return WATER_GRAVITY_LEGACY; }
            @Override public double waterFriction(boolean sprinting) { return WATER_FRICTION; }
            @Override public boolean zeroesAgainstWall() { return true; }
            @Override public boolean pushesInLava() { return false; }
        };

        Model MODERN = new Model() {
            @Override public Vec impulse(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid, double scale, double movX, double movZ) {
                return modernImpulse(inst, pos, box, fluid, scale, movX, movZ);
            }
            @Override public double waterGravity(boolean sprinting) { return sprinting ? 0.0 : WATER_GRAVITY_MODERN; }
            @Override public double waterFriction(boolean sprinting) { return sprinting ? MODERN_SPRINT_FRICTION : WATER_FRICTION; }
            @Override public boolean zeroesAgainstWall() { return false; }
            @Override public boolean pushesInLava() { return true; }
        };
    }

    /** Vanilla {@code AABB.shrink} skin on the water box. */
    private static final double INSET = 0.001;
    /** Below this fluid height the per-cell flow is scaled by the height. */
    private static final double MODERN_SHALLOW = 0.4;
    private static final double MODERN_MIN_IMPULSE = 0.0045;
    /** Per-axis velocity gating the minimum impulse. */
    private static final double MODERN_STILL = 0.003;
    /** Vanilla water box vertical inset ({@code grow(0,-0.4,0)}). */
    private static final double Y_SHRINK = 0.4;
    /** {@code BlockFluids.h} falling-water down-term, added before the final normalize. */
    private static final double FALL_DOWN = -6.0;
    private static final int[][] HORIZONTAL = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    /** {@code EntityLiving.travel}; both 1.8 + 26 walk. */
    private static final double WATER_FRICTION = 0.8;
    /** {@code travelInWater} sprint-swimming, not the {@code 0.8} water slowdown. */
    private static final double MODERN_SPRINT_FRICTION = 0.9;
    private static final double WATER_GRAVITY_LEGACY = 0.02;
    /** {@code getFluidFallingAdjustedMovement}: {@code baseGravity/16}. */
    private static final double WATER_GRAVITY_MODERN = 0.005;

    private FluidFlow() {}

    /**
     * Unit fluid-flow direction (b/t), or {@link Vec#ZERO} in still / pure-source fluid - a full source pool has no
     * slope, which is why static water folds 1:1. Summed per-cell unit slopes are re-normalized, so the magnitude is
     * flat regardless of depth.
     */
    private static Vec legacyFlow(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid) {
        Vec flow = contactScan(inst, pos, box, fluid);
        return flow == null ? Vec.ZERO : flow;
    }

    /** 1.8 item water current: {@code EntityItem} scans the RAW box (the player Y-inset would invert a 0.25-tall box and scan nothing). */
    public static Vec itemLegacyFlow(MechanicsWorld inst, Point pos, BoundingBox box) {
        Vec flow = legacyScan(inst,
                pos.x() + box.minX(), pos.y() + box.minY(), pos.z() + box.minZ(),
                pos.x() + box.maxX(), pos.y() + box.maxY(), pos.z() + box.maxZ(), Block.WATER);
        return flow == null ? Vec.ZERO : flow;
    }

    /** 1.8 {@code Entity.W()} water contact. {@code box} is the VANILLA entity box, not the physics box.
     *  {@code null} = dry, else the unit current ({@link Vec#ZERO} in still water). */
    public static @Nullable Vec waterContact(MechanicsWorld inst, Point pos, BoundingBox box) {
        return contactScan(inst, pos, box, Block.WATER);
    }

    /** {@code bb.grow(0,-0.4,0).shrink(0.001)}, quirks included: it inverts on short boxes, so detection flickers
     *  with height exactly like vanilla. */
    private static @Nullable Vec contactScan(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid) {
        return legacyScan(inst,
                pos.x() + box.minX() + INSET, pos.y() + box.minY() + Y_SHRINK + INSET, pos.z() + box.minZ() + INSET,
                pos.x() + box.maxX() - INSET, pos.y() + box.maxY() - Y_SHRINK - INSET, pos.z() + box.maxZ() - INSET,
                fluid);
    }

    private static @Nullable Vec legacyScan(MechanicsWorld inst, double ax, double ay, double az, double dx, double dy, double dz, Block fluid) {
        int xi = floor(ax), xj = floor(dx + 1.0);
        int yi = floor(ay), yj = floor(dy + 1.0);
        int zi = floor(az), zj = floor(dz + 1.0);

        boolean touched = false;
        double fx = 0, fy = 0, fz = 0;
        for (int x = xi; x < xj; x++) {
            for (int y = yi; y < yj; y++) {
                for (int z = zi; z < zj; z++) {
                    if (effLevel(inst, x, y, z, fluid) < 0) continue;
                    touched = true;
                    Vec slope = slopeAt(inst, x, y, z, fluid);
                    fx += slope.x();
                    fy += slope.y();
                    fz += slope.z();
                }
            }
        }
        if (!touched) return null;
        Vec sum = new Vec(fx, fy, fz);
        return sum.isZero() ? Vec.ZERO : sum.normalize();
    }

    /**
     * 26.1 player fluid impulse ({@code EntityFluidInteraction.applyCurrentTo}). Unlike {@link #legacyFlow}, each cell's
     * unit slope is scaled by the fluid height when shallow, and the cells are <em>averaged</em> over the fluid-cell
     * count (zero-flow source cells dilute it) rather than normalized.
     */
    private static Vec modernImpulse(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid, double scale, double movX, double movZ) {
        double feetY = pos.y() + box.minY();
        int xi = floor(pos.x() + box.minX()), xj = floor(pos.x() + box.maxX());
        int yi = floor(feetY), yj = (int) Math.ceil(pos.y() + box.maxY()) - 1;
        int zi = floor(pos.z() + box.minZ()), zj = floor(pos.z() + box.maxZ());

        double maxHeight = 0.0;
        for (int x = xi; x <= xj; x++)
            for (int y = yi; y <= yj; y++)
                for (int z = zi; z <= zj; z++) {
                    int lvl = rawLevel(inst, x, y, z, fluid);
                    if (lvl < 0) continue;
                    double top = y + fluidHeight(lvl);
                    if (top >= feetY) maxHeight = Math.max(maxHeight, top - feetY);
                }
        if (maxHeight <= 0.0) return Vec.ZERO;
        double heightScale = maxHeight < MODERN_SHALLOW ? maxHeight : 1.0;

        double sx = 0, sy = 0, sz = 0;
        int count = 0;
        for (int x = xi; x <= xj; x++)
            for (int y = yi; y <= yj; y++)
                for (int z = zi; z <= zj; z++) {
                    int lvl = rawLevel(inst, x, y, z, fluid);
                    if (lvl < 0) continue;
                    if (y + fluidHeight(lvl) < feetY) continue;
                    count++;
                    Vec slope = slopeAt(inst, x, y, z, fluid);
                    sx += slope.x() * heightScale;
                    sy += slope.y() * heightScale;
                    sz += slope.z() * heightScale;
                }
        if (count == 0) return Vec.ZERO;
        Vec impulse = new Vec(sx / count, sy / count, sz / count).mul(scale);
        if (Math.abs(movX) < MODERN_STILL && Math.abs(movZ) < MODERN_STILL
                && impulse.length() < MODERN_MIN_IMPULSE && !impulse.isZero()) {
            impulse = impulse.normalize().mul(MODERN_MIN_IMPULSE);
        }
        return impulse;
    }

    /**
     * 26.1 non-player fluid pass ({@code EntityFluidInteraction.update}): {@code height} = max fluid top above the box
     * bottom, plus the accumulated current - normalized by {@link ItemSample#current}, not player-averaged.
     */
    public static ItemSample itemModernSample(MechanicsWorld inst, Point pos, BoundingBox box, Block fluid) {
        // vanilla scans bb.deflate(0.001) but measures height from the raw bb bottom
        double minX = pos.x() + box.minX() + INSET, maxX = pos.x() + box.maxX() - INSET;
        double minY = pos.y() + box.minY() + INSET, maxY = pos.y() + box.maxY() - INSET;
        double minZ = pos.z() + box.minZ() + INSET, maxZ = pos.z() + box.maxZ() - INSET;
        int x0 = floor(minX), x1 = (int) Math.ceil(maxX) - 1;
        int y0 = floor(minY), y1 = (int) Math.ceil(maxY) - 1;
        int z0 = floor(minZ), z1 = (int) Math.ceil(maxZ) - 1;
        double feetY = pos.y() + box.minY();

        double height = 0;
        double sx = 0, sy = 0, sz = 0;
        int count = 0;
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++) {
                    int lvl = rawLevel(inst, x, y, z, fluid);
                    if (lvl < 0) continue;
                    // same fluid above -> the cell is brim-full (FlowingFluid.getHeight)
                    double top = y + (rawLevel(inst, x, y + 1, z, fluid) >= 0 ? 1.0 : fluidHeight(lvl));
                    if (top < minY) continue;
                    height = Math.max(top - feetY, height);
                    Vec slope = slopeAt(inst, x, y, z, fluid);
                    double s = height < MODERN_SHALLOW ? height : 1.0; // vanilla scales by the RUNNING max
                    sx += slope.x() * s;
                    sy += slope.y() * s;
                    sz += slope.z() * s;
                    count++;
                }
        return count == 0 ? ItemSample.EMPTY : new ItemSample(height, new Vec(sx, sy, sz), count);
    }

    public record ItemSample(double height, Vec flowSum, int cells) {
        public static final ItemSample EMPTY = new ItemSample(0, Vec.ZERO, 0);

        /** {@code Tracker.applyCurrentTo} non-player branch: normalized sum x {@code scale}, near-still floor. */
        public Vec current(double scale, double movX, double movZ) {
            if (cells == 0 || flowSum.lengthSquared() < 1.0E-5F) return Vec.ZERO;
            Vec impulse = flowSum.normalize().mul(scale);
            if (Math.abs(movX) < MODERN_STILL && Math.abs(movZ) < MODERN_STILL && impulse.length() < MODERN_MIN_IMPULSE) {
                impulse = impulse.normalize().mul(MODERN_MIN_IMPULSE);
            }
            return impulse;
        }
    }

    /** Modern fluid render height; falling/{@code >=8} is ~full. */
    private static double fluidHeight(int level) {
        return level >= 8 ? 0.8888889 : (8 - level) / 9.0;
    }

    /** Per-cell unit slope ({@code BlockFluids.h} / {@code FlowingFluid.getFlow}): neighbour level gradient + falling down-term. */
    private static Vec slopeAt(MechanicsWorld inst, int x, int y, int z, Block fluid) {
        int raw = rawLevel(inst, x, y, z, fluid);
        int i = raw >= 8 ? 0 : raw; // source/falling -> 0
        double sx = 0, sz = 0;
        for (int[] dir : HORIZONTAL) {
            int nx = x + dir[0], nz = z + dir[1];
            int j = effLevel(inst, nx, y, nz, fluid);
            if (j < 0) {
                // non-fluid neighbour with fluid below pulls toward the drop
                if (!solid(inst, nx, y, nz)) {
                    int below = effLevel(inst, nx, y - 1, nz, fluid);
                    if (below >= 0) {
                        int k = below - (i - 8);
                        sx += dir[0] * k;
                        sz += dir[1] * k;
                    }
                }
            } else {
                int k = j - i;
                sx += dir[0] * k;
                sz += dir[1] * k;
            }
        }

        Vec v = new Vec(sx, 0, sz);
        // falling fluid against a solid face adds the (0,-6,0) waterfall pull
        if (raw >= 8) {
            for (int[] dir : HORIZONTAL) {
                int nx = x + dir[0], nz = z + dir[1];
                if (solid(inst, nx, y, nz) || solid(inst, nx, y + 1, nz)) {
                    v = (v.isZero() ? v : v.normalize()).add(0, FALL_DOWN, 0);
                    break;
                }
            }
        }
        return v.isZero() ? Vec.ZERO : v.normalize();
    }

    /** Vanilla {@code f()}: -1 if the cell is not {@code fluid}, else the flow level ({@code level >= 8 -> 0}). */
    private static int effLevel(MechanicsWorld inst, int x, int y, int z, Block fluid) {
        int raw = rawLevel(inst, x, y, z, fluid);
        if (raw < 0) return -1;
        return raw >= 8 ? 0 : raw;
    }

    // level by state id: the string property lookup + parseInt otherwise runs per cell in every fluid scan
    private static final int WATER_BASE = minStateId(Block.WATER), LAVA_BASE = minStateId(Block.LAVA);
    private static final byte[] WATER_LEVELS = levelTable(Block.WATER), LAVA_LEVELS = levelTable(Block.LAVA);

    private static int minStateId(Block fluid) {
        int min = Integer.MAX_VALUE;
        for (Block state : fluid.possibleStates()) min = Math.min(min, state.stateId());
        return min;
    }

    private static byte[] levelTable(Block fluid) {
        var states = fluid.possibleStates();
        byte[] table = new byte[states.size()];
        int base = minStateId(fluid);
        for (Block state : states) {
            String level = state.getProperty("level");
            table[state.stateId() - base] = level == null ? 0 : Byte.parseByte(level);
        }
        return table;
    }

    /** The {@code fluid}'s {@code level} property (0 source, 1-7 flowing, 8-15 falling), or -1 when the cell is not that fluid. */
    private static int rawLevel(MechanicsWorld inst, int x, int y, int z, Block fluid) {
        if (!inst.isChunkLoaded(x >> 4, z >> 4)) return -1;
        Block block = inst.getBlock(x, y, z); // full state so the level property is present
        if (block == null || !block.compare(fluid)) return -1; // by id: matches every level
        boolean water = fluid.compare(Block.WATER);
        int base = water ? WATER_BASE : LAVA_BASE;
        byte[] table = water ? WATER_LEVELS : LAVA_LEVELS;
        int idx = block.stateId() - base;
        return idx >= 0 && idx < table.length ? table[idx] : 0;
    }

    /** Approximates vanilla's {@code material.isSolid()} / face-occlusion test with the registry solid flag. */
    private static boolean solid(MechanicsWorld inst, int x, int y, int z) {
        if (!inst.isChunkLoaded(x >> 4, z >> 4)) return false;
        Block block = inst.getBlock(x, y, z, Block.Getter.Condition.TYPE);
        return block != null && block.isSolid();
    }

    private static int floor(double v) {
        return (int) Math.floor(v);
    }
}
