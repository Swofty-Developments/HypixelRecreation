package io.github.term4.polyp.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/** Direction math helpers: unit vectors, yaw convention, blends. */
public final class Directions {

    private Directions() {}

    public static final Vec UP = new Vec(0, 1, 0);

    /** Length below which a direction is degenerate (falls back to a random/up direction). */
    public static final double EPSILON = 1e-6;

    /** Minecraft yaw -> horizontal facing unit vector. Convention: yaw 0 -> +Z, 90 -> -X, -90 -> +X, 180 -> -Z. */
    public static Vec fromYaw(double yawDeg) {
        double rad = Math.toRadians(yawDeg);
        return new Vec(-Math.sin(rad), 0, Math.cos(rad));
    }

    /** Minecraft yaw (degrees) of a direction/velocity vector (yaw 0 -&gt; +Z). */
    public static float yaw(Vec dir) {
        return (float) Math.toDegrees(Math.atan2(dir.x(), dir.z()));
    }

    /** Horizontal unit vector 90° clockwise of {@code yawDeg} (the shooter's right hand). */
    public static Vec rightOf(double yawDeg) {
        double rad = Math.toRadians(yawDeg);
        return new Vec(-Math.cos(rad), 0, -Math.sin(rad));
    }

    /** Rotates {@code v} around the Y axis by {@code degrees} (y untouched). */
    public static Vec rotateY(Vec v, double degrees) {
        double rad = Math.toRadians(degrees), cos = Math.cos(rad), sin = Math.sin(rad);
        return new Vec(v.x() * cos - v.z() * sin, v.y(), v.x() * sin + v.z() * cos);
    }

    /**
     * Vanilla {@code shootFromRotation} heading (identical 1.8-26): horizontal from the UN-offset pitch, vertical from
     * {@code pitch + pitchOffsetDeg} - NOT a rotation of the look vector (the potion's -20° keeps its full horizontal reach).
     */
    public static Vec headingWithPitchOffset(double yawDeg, double pitchDeg, double pitchOffsetDeg) {
        double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg);
        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(Math.toRadians(pitchDeg + pitchOffsetDeg));
        double z = Math.cos(yaw) * Math.cos(pitch);
        return new Vec(x, y, z).normalize();
    }

    /** Unit vector of {@code (dx, dy, dz)}, or {@code null} when its length is under {@code epsilon}. */
    public static @Nullable Vec unit3D(double dx, double dy, double dz, double epsilon) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < epsilon) return null;
        return new Vec(dx / len, dy / len, dz / len);
    }

    /** Minecraft pitch (degrees) of a direction/velocity vector: {@code atan2(y, horizontalLength)}
     *  (projectile convention, from 1.8 {@code EntityArrow} rotation-from-motion). */
    public static float pitch(Vec dir) {
        double hl = Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
        return (float) Math.toDegrees(Math.atan2(dir.y(), hl));
    }

    /**
     * Snaps a horizontal vector onto its dominant cardinal axis, components in {@code {-1, 0, +1}}. Ties favour x; a
     * zero vector snaps to {@code (0, 0)}.
     */
    public static Vec snapDominantAxis(Vec v) {
        return Math.abs(v.x()) >= Math.abs(v.z())
                ? new Vec(Math.signum(v.x()), 0, 0)
                : new Vec(0, 0, Math.signum(v.z()));
    }

    /** Horizontal (xz) unit direction from {@code from} to {@code to}; {@link #randomHorizontal()} when degenerate. */
    public static Vec horizontalBetween(Point from, Point to) {
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < EPSILON) return randomHorizontal();
        return new Vec(dx / dist, 0, dz / dist);
    }

    /** Vertical unit direction (+-y) from the height delta {@code from -> to}; {@link #UP} when level. */
    public static Vec verticalBetween(Point from, Point to) {
        double dy = to.y() - from.y();
        if (Math.abs(dy) < EPSILON) return UP;
        return new Vec(0, Math.signum(dy), 0);
    }

    /** Horizontal (xz) unit direction of a 3D unit vector; {@link #randomHorizontal()} when looking straight up/down. */
    public static Vec horizontalOf(Vec dir) {
        double len = Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
        if (len < EPSILON) return randomHorizontal();
        return new Vec(dir.x() / len, 0, dir.z() / len);
    }

    /** Vertical unit direction (+-y) of a 3D unit vector; {@link #UP} when horizontal. */
    public static Vec verticalOf(Vec dir) {
        if (Math.abs(dir.y()) < EPSILON) return UP;
        return new Vec(0, Math.signum(dir.y()), 0);
    }

    /** Random horizontal (xz) unit vector. */
    public static Vec randomHorizontal() {
        Vec v;
        do {
            double x = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            double z = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            v = new Vec(x, 0, z);
        } while (v.length() < EPSILON);
        return v.normalize();
    }

    /** Weighted blend of two vectors, normalized; {@code fallback} when the weights or the sum are degenerate. */
    public static Vec blend(Vec a, Vec b, double wA, double wB, Supplier<Vec> fallback) {
        if (wA <= 0 && wB <= 0) return fallback.get();
        Vec sum = a.mul(wA).add(b.mul(wB));
        return sum.lengthSquared() < EPSILON * EPSILON ? fallback.get() : sum.normalize();
    }
}
