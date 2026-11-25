package io.github.term4.polyp.util;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Teleports that don't make a legacy client's proxy invent packets. Minestom's 3-arg {@code teleport} always ORs in
 * {@code DELTA_COORD}; a pre-1.21.2 client has no field for relative delta movement, so ViaBackwards fakes one with
 * an explosion packet at {@code (0, 20000, 0)} - one per teleport. 1.8 needs none of it, since an absolute position
 * packet already zeroes the client's motion.
 */
public final class Teleports {

    private Teleports() {}

    /** {@code flags} are the position/view relative flags; the delta is absolute for legacy clients. */
    public static void place(Entity entity, Pos target, int flags) {
        ClientInfoTracker clientInfo = Polyp.getInstance().clientInfo();
        if (entity instanceof Player p && clientInfo != null && clientInfo.isLegacy(p)) {
            entity.teleport(target, Vec.ZERO, null, flags);
        } else {
            entity.teleport(target, null, flags);
        }
    }
}
