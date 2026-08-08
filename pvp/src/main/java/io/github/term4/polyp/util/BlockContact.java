package io.github.term4.polyp.util;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.world.DimensionType;

import java.util.function.Predicate;

/**
 * Block occupancy / collision-shape contact tests for hazard producers.
 *
 * <p>{@link #scan}/{@link #touching} walk full block cells - for fluids, fire, and 1.8 {@code doBlockCollisions}
 * contacts (cactus). {@link #scanShapes}/{@link #touchingShapes} test real registry collision shapes only
 * (shapeless fluids/fire never contact) - for box-fit queries. Unloaded chunks and out-of-range Y are skipped.
 */
public final class BlockContact {

    /** Vanilla contact skin: the bounding box is inset by this much on every axis before tests. */
    public static final double VANILLA_INSET = 0.001;

    private BlockContact() {}

    /** Vanilla 1.8 climbable set: ladder or vine at the feet block. */
    public static boolean climbing(Entity entity) {
        if (entity.getInstance() == null) return false;
        Block feet = MechanicsWorld.viewed(entity).getBlock(entity.getPosition(), Block.Getter.Condition.TYPE);
        return feet != null && (feet.compare(Block.LADDER) || feet.compare(Block.VINE));
    }

    /** True when any overlapped cell matches (vanilla inset). */
    public static boolean touching(Entity entity, Predicate<Block> match) {
        return touching(entity, VANILLA_INSET, match);
    }

    public static boolean touching(Entity entity, double inset, Predicate<Block> match) {
        return scan(entity, inset, match);
    }

    /** Visits overlapped cells with the vanilla inset until {@code visitor} returns {@code true}. */
    public static boolean scan(Entity entity, Predicate<Block> visitor) {
        return scan(entity, VANILLA_INSET, visitor);
    }

    /** Visits overlapped cells until {@code visitor} returns {@code true}. */
    public static boolean scan(Entity entity, double inset, Predicate<Block> visitor) {
        if (entity.getInstance() == null) return false;
        // contacts follow the VIEWED world (a spectator's), not the base
        return scanCells(MechanicsWorld.viewed(entity), entity.getPosition(), entity.getBoundingBox(), inset, visitor);
    }

    /** Cell walk with vanilla inset. */
    public static boolean scan(MechanicsWorld world, Point position, BoundingBox box, Predicate<Block> visitor) {
        return scanCells(world, position, box, VANILLA_INSET, visitor);
    }

    /**
     * Visits each block cell overlapped by {@code box} at {@code position} (after {@code inset}),
     * until {@code visitor} returns {@code true}.
     */
    public static boolean scanCells(MechanicsWorld world, Point position, BoundingBox box,
                                    double inset, Predicate<Block> visitor) {
        if (inset > 0) box = inset(box, inset);
        DimensionType dim = world.dimension();
        int minY = dim.minY();
        int maxY = dim.minY() + dim.height() - 1;

        var it = box.getBlocks(position);
        while (it.hasNext()) {
            var p = it.next();
            int x = p.blockX(), y = p.blockY(), z = p.blockZ();
            if (y < minY || y > maxY) continue;
            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
            Block block = world.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block != null && visitor.test(block)) return true;
        }
        return false;
    }

    /** True when the entity intersects any block whose shape matches (vanilla inset). */
    public static boolean touchingShapes(Entity entity, Predicate<Block> match) {
        return touchingShapes(entity, VANILLA_INSET, match);
    }

    public static boolean touchingShapes(Entity entity, double inset, Predicate<Block> match) {
        return scanShapes(entity, inset, hit -> match.test(hit.block()));
    }

    /** Visits shape intersections with the vanilla inset until {@code visitor} returns {@code true}. */
    public static boolean scanShapes(Entity entity, Predicate<BlockContactHit> visitor) {
        return scanShapes(entity, VANILLA_INSET, visitor);
    }

    /**
     * Tests registry collision shapes for every cell the entity box overlaps. Each hit includes the
     * contacted {@link BlockFace} (shallowest-axis separation).
     */
    public static boolean scanShapes(Entity entity, double inset, Predicate<BlockContactHit> visitor) {
        if (entity.getInstance() == null) return false;
        return scanShapes(MechanicsWorld.viewed(entity), entity.getPosition(), entity.getBoundingBox(), inset, visitor);
    }

    public static boolean scanShapes(MechanicsWorld world, Point position, BoundingBox box,
                                     double inset, Predicate<BlockContactHit> visitor) {
        if (inset > 0) box = inset(box, inset);
        DimensionType dim = world.dimension();
        int minY = dim.minY();
        int maxY = dim.minY() + dim.height() - 1;

        double ex0 = position.x() + box.minX();
        double ey0 = position.y() + box.minY();
        double ez0 = position.z() + box.minZ();
        double ex1 = position.x() + box.maxX();
        double ey1 = position.y() + box.maxY();
        double ez1 = position.z() + box.maxZ();

        var it = box.getBlocks(position);
        while (it.hasNext()) {
            var p = it.next();
            int x = p.blockX(), y = p.blockY(), z = p.blockZ();
            if (y < minY || y > maxY) continue;
            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

            Block block = world.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            if (block == null || block.isAir()) continue;

            Point blockOrigin = new Vec(x, y, z);
            Point relative = position.sub(blockOrigin);
            Shape shape = block.registry().collisionShape();

            // real collision shapes only: shapeless blocks (fire, fluids) never contact - cell semantics live in scan()
            if (shape == null || shape.relativeStart().samePoint(shape.relativeEnd())) continue;
            if (!shape.intersectBox(relative, box)) continue;

            BlockFace face = contactFace(ex0, ey0, ez0, ex1, ey1, ez1, shape, blockOrigin);
            if (visitor.test(new BlockContactHit(x, y, z, block, face))) return true;
        }
        return false;
    }

    private static BlockFace contactFace(double ex0, double ey0, double ez0, double ex1, double ey1, double ez1,
                                         Shape shape, Point blockOrigin) {
        double bx0 = blockOrigin.x();
        double by0 = blockOrigin.y();
        double bz0 = blockOrigin.z();
        double bx1 = bx0 + 1.0;
        double by1 = by0 + 1.0;
        double bz1 = bz0 + 1.0;

        if (shape != null) {
            Point relStart = shape.relativeStart();
            Point relEnd = shape.relativeEnd();
            double px0 = bx0 + relStart.x();
            double py0 = by0 + relStart.y();
            double pz0 = bz0 + relStart.z();
            double px1 = bx0 + relEnd.x();
            double py1 = by0 + relEnd.y();
            double pz1 = bz0 + relEnd.z();
            if (intersects(ex0, ey0, ez0, ex1, ey1, ez1, px0, py0, pz0, px1, py1, pz1)) {
                return shallowestFace(ex0, ey0, ez0, ex1, ey1, ez1, px0, py0, pz0, px1, py1, pz1).face();
            }
        }
        return shallowestFace(ex0, ey0, ez0, ex1, ey1, ez1, bx0, by0, bz0, bx1, by1, bz1).face();
    }

    private record FaceOverlap(BlockFace face, double overlap) {}

    private static FaceOverlap shallowestFace(double ex0, double ey0, double ez0, double ex1, double ey1, double ez1,
                                              double bx0, double by0, double bz0, double bx1, double by1, double bz1) {
        double ox = Math.min(ex1, bx1) - Math.max(ex0, bx0);
        double oy = Math.min(ey1, by1) - Math.max(ey0, by0);
        double oz = Math.min(ez1, bz1) - Math.max(ez0, bz0);

        double ecx = (ex0 + ex1) * 0.5;
        double ecy = (ey0 + ey1) * 0.5;
        double ecz = (ez0 + ez1) * 0.5;
        double bcx = (bx0 + bx1) * 0.5;
        double bcy = (by0 + by1) * 0.5;
        double bcz = (bz0 + bz1) * 0.5;

        if (ox <= oy && ox <= oz) {
            return new FaceOverlap(ecx < bcx ? BlockFace.WEST : BlockFace.EAST, ox);
        }
        if (oy <= oz) {
            return new FaceOverlap(ecy < bcy ? BlockFace.BOTTOM : BlockFace.TOP, oy);
        }
        return new FaceOverlap(ecz < bcz ? BlockFace.NORTH : BlockFace.SOUTH, oz);
    }

    private static boolean intersects(double ax0, double ay0, double az0, double ax1, double ay1, double az1,
                                    double bx0, double by0, double bz0, double bx1, double by1, double bz1) {
        return ax0 < bx1 && ax1 > bx0 && ay0 < by1 && ay1 > by0 && az0 < bz1 && az1 > bz0;
    }

    /** Shrinks {@code box} by {@code skin} on every axis (vanilla {@code AABB.shrink}). */
    public static BoundingBox inset(BoundingBox box, double skin) {
        if (skin <= 0) return box;
        return new BoundingBox(
                box.relativeStart().add(skin, skin, skin),
                box.relativeEnd().sub(skin, skin, skin)
        );
    }

    /** Grows {@code box} by {@code amount} on every axis (vanilla {@code AABB.grow}). */
    public static BoundingBox grow(BoundingBox box, double amount) {
        if (amount <= 0) return box;
        return new BoundingBox(
                box.relativeStart().sub(amount, amount, amount),
                box.relativeEnd().add(amount, amount, amount)
        );
    }

    /**
     * Per-axis grow/shrink from vanilla's asymmetric fluid checks (e.g. lava
     * {@code grow(-0.1, -0.4, -0.1)}). Negative values shrink.
     */
    public static BoundingBox adjust(BoundingBox box, double dx, double dy, double dz) {
        return new BoundingBox(
                box.relativeStart().sub(dx, dy, dz),
                box.relativeEnd().add(dx, dy, dz)
        );
    }

    /** Collision shape covers all six faces (dirt/stone yes, stairs/slabs/fences no); false without a collision shape. */
    public static boolean isFullCube(Block block) {
        Shape shape = block.registry().collisionShape();
        if (shape == null) return false;
        for (BlockFace face : BlockFace.values()) {
            if (!shape.isFaceFull(face)) return false;
        }
        return true;
    }

    /** Whether an entity can occupy {@code block}'s space - the inverse of Minecraft's {@code blocksMotion}. */
    public static boolean isPassable(Block block) {
        return !block.blocksMotion();
    }
}
