package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.util.geometry.Sightline;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla {@code getSeenPercent}: the fraction of a point grid across the entity's box with line of sight to the
 * center. Both vanillas share the grid; the RAY differs (1.8 {@code rayTraceBlocks} vs the modern voxel clip), and
 * they disagree on boundary-riding/grazing rays - visible as TNT sheltering behind block edges.
 */
public final class ExplosionExposure {

    private ExplosionExposure() {}

    /**
     * Ray implementation; {@code NONE} = full exposure for everyone in range. {@code LEGACY_1_8} intersects the
     * block's real shape (rays pass over a slab, so 1.8 pushes off-flat); {@code LEGACY_1_8_FULL_CUBE} treats every
     * solid as a full cube (the Hypixel/MineMen divergence - an off-flat blast is fully occluded).
     */
    public enum Rays { NONE, MODERN, LEGACY_1_8, LEGACY_1_8_FULL_CUBE }

    // 1.8 Vec3D intermediate epsilon ((float) 1e-7 widened)
    private static final double EPSILON_1_8 = 1.0000000116860974E-7;

    /** Sample-point grid over the entity's box: vanilla's per-axis step {@code 1/(size*2+1)} + the x/z centering offset. */
    private record Grid(double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                        double xs, double ys, double zs, double xOff, double zOff) {
        static @Nullable Grid of(Entity entity) {
            BoundingBox bb = entity.getBoundingBox();
            Point pos = entity.getPosition();
            double minX = pos.x() + bb.minX(), maxX = pos.x() + bb.maxX();
            double minY = pos.y() + bb.minY(), maxY = pos.y() + bb.maxY();
            double minZ = pos.z() + bb.minZ(), maxZ = pos.z() + bb.maxZ();
            double xs = 1.0 / ((maxX - minX) * 2.0 + 1.0);
            double ys = 1.0 / ((maxY - minY) * 2.0 + 1.0);
            double zs = 1.0 / ((maxZ - minZ) * 2.0 + 1.0);
            if (xs < 0.0 || ys < 0.0 || zs < 0.0) return null; // vanilla degenerate-box guard
            return new Grid(minX, maxX, minY, maxY, minZ, maxZ, xs, ys, zs,
                    (1.0 - Math.floor(1.0 / xs) * xs) / 2.0, (1.0 - Math.floor(1.0 / zs) * zs) / 2.0);
        }
    }

    /** Modern-flavoured exposure: the vanilla grid over Minestom's swept collision (approximates 26.1 {@code clip}). */
    public static float seenPercent(MechanicsWorld world, Point center, Entity entity) {
        Grid g = Grid.of(entity);
        if (g == null) return 0.0f;
        int hits = 0, count = 0;
        for (double xx = 0.0; xx <= 1.0; xx += g.xs) {
            for (double yy = 0.0; yy <= 1.0; yy += g.ys) {
                for (double zz = 0.0; zz <= 1.0; zz += g.zs) {
                    Vec from = new Vec(lerp(xx, g.minX, g.maxX) + g.xOff, lerp(yy, g.minY, g.maxY), lerp(zz, g.minZ, g.maxZ) + g.zOff);
                    if (!Sightline.blocked(world, from, center)) hits++;
                    count++;
                }
            }
        }
        return count == 0 ? 0.0f : (float) hits / count;
    }

    /**
     * Faithful 1.8 exposure: {@code World.rayTraceBlocks} (paper 1.8.8) against each block's real collision box.
     * Where the 1.8 ray differs from any swept trace: a level ray riding a block seam marches in the AIR row (flat
     * ground reads full), a DESCENDING boundary-sample ray steps into the block below (the {@code -0.0} quirk), and
     * the endpoint's own block never blocks.
     */
    public static float seenPercent18(MechanicsWorld world, Point center, Entity entity) {
        return legacyExposure(world, center, entity, true);
    }

    /** 1.8 exposure over full-cube solids - the Hypixel/MineMen off-flat gate. Capture-verified against MineMen ray-for-ray (k/27). */
    public static float seenPercent18FullCube(MechanicsWorld world, Point center, Entity entity) {
        return legacyExposure(world, center, entity, false);
    }

    private static float legacyExposure(MechanicsWorld world, Point center, Entity entity, boolean shaped) {
        Grid g = Grid.of(entity);
        if (g == null) return 0.0f;
        int clear = 0, total = 0;
        // float accumulators like 1.8 (the loop's sample count is part of the parity)
        for (float f = 0.0F; f <= 1.0F; f = (float) (f + g.xs)) {
            for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) (f1 + g.ys)) {
                for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) (f2 + g.zs)) {
                    double sx = g.minX + (g.maxX - g.minX) * f + g.xOff;
                    double sy = g.minY + (g.maxY - g.minY) * f1;
                    double sz = g.minZ + (g.maxZ - g.minZ) * f2 + g.zOff;
                    if (!rayHits18(world, shaped, sx, sy, sz, center.x(), center.y(), center.z())) clear++;
                    total++;
                }
            }
        }
        return total == 0 ? 0.0f : (float) clear / total;
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    // 1.8 World.rayTraceBlocks: start-block check, boundary march with the -0.0 -> -1e-4 quirk, terminates
    // unchecked when the march reaches the end block
    private static boolean rayHits18(MechanicsWorld in, boolean shaped, double ax, double ay, double az, double bx, double by, double bz) {
        int i = (int) Math.floor(bx), j = (int) Math.floor(by), k = (int) Math.floor(bz);
        int l = (int) Math.floor(ax), i1 = (int) Math.floor(ay), j1 = (int) Math.floor(az);
        if (blockHit(in, shaped, l, i1, j1, ax, ay, az, bx, by, bz)) return true;
        int n = 200;
        while (n-- >= 0) {
            if (l == i && i1 == j && j1 == k) return false;
            boolean sx = true, sy = true, sz = true;
            double px = 999, py = 999, pz = 999;
            if (i > l) px = l + 1.0; else if (i < l) px = l; else sx = false;
            if (j > i1) py = i1 + 1.0; else if (j < i1) py = i1; else sy = false;
            if (k > j1) pz = j1 + 1.0; else if (k < j1) pz = j1; else sz = false;
            double tx = 999, ty = 999, tz = 999;
            double dx = bx - ax, dy = by - ay, dz = bz - az;
            if (sx) tx = (px - ax) / dx;
            if (sy) ty = (py - ay) / dy;
            if (sz) tz = (pz - az) / dz;
            if (tx == -0.0D) tx = -1.0E-4D;
            if (ty == -0.0D) ty = -1.0E-4D;
            if (tz == -0.0D) tz = -1.0E-4D;
            int face;
            if (tx < ty && tx < tz) { face = 0; ax = px; ay += dy * tx; az += dz * tx; }
            else if (ty < tz) { face = 1; ax += dx * ty; ay = py; az += dz * ty; }
            else { face = 2; ax += dx * tz; ay += dy * tz; az = pz; }
            l = (int) Math.floor(ax) - (face == 0 && i < l ? 1 : 0);
            i1 = (int) Math.floor(ay) - (face == 1 && j < i1 ? 1 : 0);
            j1 = (int) Math.floor(az) - (face == 2 && k < j1 ? 1 : 0);
            if (blockHit(in, shaped, l, i1, j1, ax, ay, az, bx, by, bz)) return true;
        }
        return false;
    }

    private static boolean blockHit(MechanicsWorld in, boolean shaped, int x, int y, int z,
                                    double ax, double ay, double az, double bx, double by, double bz) {
        Block block = in.getBlock(x, y, z);
        if (!block.isSolid()) return false;
        if (!shaped) return boxIntercept(x, y, z, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, ax, ay, az, bx, by, bz);
        // collisionShape is the overall bbox - exact for slabs/full blocks, over-approximates stairs to a cube
        Shape shape = block.registry().collisionShape();
        Point s = shape.relativeStart(), e = shape.relativeEnd();
        return boxIntercept(x, y, z, s.x(), s.y(), s.z(), e.x(), e.y(), e.z(), ax, ay, az, bx, by, bz);
    }

    // 1.8 Block.collisionRayTrace vs an axis-aligned box: face-plane intercepts with INCLUSIVE cross-axis bounds
    private static boolean boxIntercept(int bx, int by, int bz, double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ,
                                        double ax, double ay, double az, double ex, double ey, double ez) {
        ax -= bx; ay -= by; az -= bz;
        ex -= bx; ey -= by; ez -= bz;
        return xFace(ax, ay, az, ex, ey, ez, minX, minY, maxY, minZ, maxZ)
                || xFace(ax, ay, az, ex, ey, ez, maxX, minY, maxY, minZ, maxZ)
                || yFace(ax, ay, az, ex, ey, ez, minY, minX, maxX, minZ, maxZ)
                || yFace(ax, ay, az, ex, ey, ez, maxY, minX, maxX, minZ, maxZ)
                || zFace(ax, ay, az, ex, ey, ez, minZ, minX, maxX, minY, maxY)
                || zFace(ax, ay, az, ex, ey, ez, maxZ, minX, maxX, minY, maxY);
    }

    private static boolean xFace(double ax, double ay, double az, double ex, double ey, double ez,
                                 double x, double minY, double maxY, double minZ, double maxZ) {
        double d = ex - ax;
        if (d * d < EPSILON_1_8) return false;
        double t = (x - ax) / d;
        if (t < 0.0 || t > 1.0) return false;
        double y = ay + (ey - ay) * t, z = az + (ez - az) * t;
        return y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    private static boolean yFace(double ax, double ay, double az, double ex, double ey, double ez,
                                 double y, double minX, double maxX, double minZ, double maxZ) {
        double d = ey - ay;
        if (d * d < EPSILON_1_8) return false;
        double t = (y - ay) / d;
        if (t < 0.0 || t > 1.0) return false;
        double x = ax + (ex - ax) * t, z = az + (ez - az) * t;
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static boolean zFace(double ax, double ay, double az, double ex, double ey, double ez,
                                 double z, double minX, double maxX, double minY, double maxY) {
        double d = ez - az;
        if (d * d < EPSILON_1_8) return false;
        double t = (z - az) / d;
        if (t < 0.0 || t > 1.0) return false;
        double x = ax + (ex - ax) * t, y = ay + (ey - ay) * t;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
