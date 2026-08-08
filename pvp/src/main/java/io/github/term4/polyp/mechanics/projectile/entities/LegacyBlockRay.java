package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * 1.8 {@code rayTraceBlocks} parity for blocks whose collision is taller than 1 (fences, walls, gates): 1.8 rays
 * their SELECTION box - the connection envelope at height 1.0 ({@code BlockFence.updateShape}) - not the 1.5
 * collision shape. So projectiles fly over the 1.0-1.5 band the exact shape would catch, and die on envelope
 * corners the exact multi-boxes let through (gap threads). Contacts on other blocks pass through unchanged.
 */
final class LegacyBlockRay {

    private static final double EPS = 1e-7;

    private LegacyBlockRay() {}

    static PhysicsResult sweep(MechanicsWorld world, BoundingBox box, Pos position, Vec velocity,
                               @Nullable PhysicsResult last) {
        return resolve(world, box, position, velocity, last, 0);
    }

    private static PhysicsResult resolve(MechanicsWorld world, BoundingBox box, Pos from, Vec vel,
                                         @Nullable PhysicsResult last, int depth) {
        PhysicsResult modern = world.sweepLoaded(box, from, vel, last, true);
        if (vel.lengthSquared() < EPS) return modern;

        Hit legacy = trace(world, from, vel); // bottom line: fences matter at the projectile's own height
        int modernAxis = contactAxis(modern);
        boolean modernTall = modernAxis >= 0 && tall(modern.collisionShapes()[modernAxis]);
        double modernT = modernAxis >= 0
                ? modern.newPosition().sub(from).asVec().length() / vel.length() : Double.POSITIVE_INFINITY;

        if (!modernTall) {
            // the modern contact stands unless a legacy envelope is struck first
            if (legacy != null && legacy.t < modernT) return synthetic(from, vel, box, legacy, modern);
            return modern;
        }
        if (legacy != null) return synthetic(from, vel, box, legacy, modern);
        // 1.8 flies over/past this block: continue the remaining step beyond the dropped contact
        if (depth >= 2) return passThrough(modern);
        double past = Math.min(1.0, modernT + 0.05);
        PhysicsResult rest = resolve(world, box, from.add(vel.mul(past)), vel.mul(1 - past), null, depth + 1);
        return rest.hasCollision() ? rest : passThrough(modern);
    }

    private record Hit(double t, int axis, Point point, Point cell, Shape shape) {}

    /** Earliest tall-family SELECTION-box crossing of the centre segment, or {@code null}. */
    private static @Nullable Hit trace(MechanicsWorld world, Pos start, Vec vel) {
        double ex = start.x() + vel.x(), ey = start.y() + vel.y(), ez = start.z() + vel.z();
        int x0 = (int) Math.floor(Math.min(start.x(), ex)), x1 = (int) Math.floor(Math.max(start.x(), ex));
        int y0 = (int) Math.floor(Math.min(start.y(), ey)), y1 = (int) Math.floor(Math.max(start.y(), ey));
        int z0 = (int) Math.floor(Math.min(start.z(), ez)), z1 = (int) Math.floor(Math.max(start.z(), ez));
        Hit best = null;
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                    Block block = world.getBlock(x, y, z, Block.Getter.Condition.TYPE);
                    if (block == null || block.isAir()) continue;
                    Shape shape = block.registry().collisionShape();
                    if (!tall(shape) && !pane(block)) continue;
                    Point s = shape.relativeStart(), e = shape.relativeEnd();
                    Hit hit = clip(start, vel, x + s.x(), y, z + s.z(), x + e.x(), y + 1.0, z + e.z(),
                            new Vec(x, y, z), shape);
                    if (hit != null && (best == null || hit.t < best.t)) best = hit;
                }
        return best;
    }

    private static boolean tall(@Nullable Shape shape) {
        return shape != null && shape.relativeEnd().y() > 1.0;
    }

    // 1.8 BlockPane has no collisionRayTrace override either: panes ray as their connection envelope (the
    // fat square at junctions), catching corner-notch threads the exact cross shapes let through
    private static boolean pane(Block block) {
        String name = block.name();
        return name.endsWith("_pane") || name.equals("minecraft:iron_bars");
    }

    /** Slab-method segment-vs-AABB entry, {@code null} on miss or when starting inside. */
    private static @Nullable Hit clip(Pos start, Vec vel, double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ, Point cell, Shape shape) {
        double tEnter = 0, tExit = 1;
        int axis = -1;
        double[] p = {start.x(), start.y(), start.z()};
        double[] v = {vel.x(), vel.y(), vel.z()};
        double[] lo = {minX, minY, minZ};
        double[] hi = {maxX, maxY, maxZ};
        for (int i = 0; i < 3; i++) {
            if (Math.abs(v[i]) < EPS) {
                if (p[i] <= lo[i] || p[i] >= hi[i]) return null;
                continue;
            }
            double t1 = (lo[i] - p[i]) / v[i], t2 = (hi[i] - p[i]) / v[i];
            double near = Math.min(t1, t2), far = Math.max(t1, t2);
            if (near > tEnter) {
                tEnter = near;
                axis = i;
            }
            tExit = Math.min(tExit, far);
            if (tEnter > tExit) return null;
        }
        if (axis < 0) return null; // started inside: 1.8 collisionRayTrace has no inside-start hit either
        Point point = new Vec(start.x() + vel.x() * tEnter, start.y() + vel.y() * tEnter, start.z() + vel.z() * tEnter);
        return new Hit(tEnter, axis, point, cell, shape);
    }

    private static int contactAxis(PhysicsResult r) {
        if (!r.hasCollision() || r.collisionShapes() == null) return -1;
        for (int axis = 0; axis < 3; axis++) if (r.collisionShapes()[axis] != null) return axis;
        return -1;
    }

    private static PhysicsResult synthetic(Pos from, Vec vel, BoundingBox box, Hit hit, PhysicsResult modern) {
        double stop = Math.max(0, hit.t - EPS);
        Pos newPosition = from.add(vel.mul(stop));
        Point[] points = new Point[3];
        Shape[] shapes = new Shape[3];
        Point[] shapePositions = new Point[3];
        points[hit.axis] = hit.point;
        shapes[hit.axis] = hit.shape;
        shapePositions[hit.axis] = hit.cell;
        Vec newVelocity = switch (hit.axis) {
            case 0 -> vel.withX(0);
            case 1 -> vel.withY(0);
            default -> vel.withZ(0);
        };
        return new PhysicsResult(newPosition, newVelocity, hit.axis == 1 && vel.y() < 0,
                hit.axis == 0, hit.axis == 1, hit.axis == 2,
                vel, points, shapes, shapePositions, true, modern.res());
    }

    private static PhysicsResult passThrough(PhysicsResult modern) {
        return new PhysicsResult(modern.newPosition(), modern.newVelocity(), false,
                false, false, false, modern.originalDelta(),
                new Point[3], new Shape[3], new Point[3], false, modern.res());
    }
}
