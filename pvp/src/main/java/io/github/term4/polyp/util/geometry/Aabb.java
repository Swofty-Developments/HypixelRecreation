package io.github.term4.polyp.util.geometry;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;

/** An axis-aligned box in world space with the point/ray tests reach and hit detection share. */
public record Aabb(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

    /** {@code bb} placed at {@code pos}, grown {@code pad} on every side (an attacker's client-perceived target box). */
    public static Aabb of(BoundingBox bb, Point pos, double pad) {
        return new Aabb(
                pos.x() + bb.minX() - pad, pos.y() + bb.minY() - pad, pos.z() + bb.minZ() - pad,
                pos.x() + bb.maxX() + pad, pos.y() + bb.maxY() + pad, pos.z() + bb.maxZ() + pad);
    }

    /** Distance from {@code (x,y,z)} to the nearest point on this box ({@code 0} when the point is inside). */
    public double nearestDistance(double x, double y, double z) {
        double dx = x - clamp(x, minX, maxX), dy = y - clamp(y, minY, maxY), dz = z - clamp(z, minZ, maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Slab test of the ray from {@code (ox,oy,oz)} along {@code (dx,dy,dz)} (need not be unit): the entry distance in
     * world units, or {@code -1} if it misses within {@code maxDistance}. An origin inside the box returns {@code 0}.
     */
    public double rayDistance(double ox, double oy, double oz, double dx, double dy, double dz, double maxDistance) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-9) return -1;
        double inv = 1.0 / len;
        double[] t = {0.0, maxDistance};
        if (!slab(ox, dx * inv, minX, maxX, t)) return -1;
        if (!slab(oy, dy * inv, minY, maxY, t)) return -1;
        if (!slab(oz, dz * inv, minZ, maxZ, t)) return -1;
        return t[0];
    }

    /** One axis of the slab test; narrows {@code t = {tMin, tMax}} in place, returns {@code false} once the ray misses. */
    private static boolean slab(double origin, double dir, double lo, double hi, double[] t) {
        if (Math.abs(dir) > 1e-9) {
            double t1 = (lo - origin) / dir, t2 = (hi - origin) / dir;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > t[0]) t[0] = t1;
            if (t2 < t[1]) t[1] = t2;
            return t[0] <= t[1];
        }
        return origin >= lo && origin <= hi; // parallel to the slab: inside its span or never crosses
    }

    private static double clamp(double v, double lo, double hi) { return v < lo ? lo : Math.min(v, hi); }
}
