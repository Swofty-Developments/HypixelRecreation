package io.github.term4.polyp.util.geometry;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

/**
 * Line-of-sight obstruction: whether solid terrain blocks the straight segment between two points. A zero-size box swept
 * along the segment is a block raycast through Minestom's collision engine - the primitive vanilla exposure uses.
 */
public final class Sightline {

    private static final BoundingBox POINT = new BoundingBox(Vec.ZERO, Vec.ZERO);

    private Sightline() {}

    /** Whether solid terrain blocks the segment {@code from -> to} (unloaded chunks read as solid). */
    public static boolean blocked(MechanicsWorld world, Point from, Point to) {
        Vec dir = new Vec(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
        if (dir.isZero()) return false;
        Pos origin = from instanceof Pos p ? p : new Pos(from.x(), from.y(), from.z());
        return world.sweep(POINT, origin, dir, null, false).hasCollision();
    }
}
