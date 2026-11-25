package io.github.term4.polyp.platform.fixes;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Minestom's {@code Entity.refreshPosition} skips the whole update when the target equals its (possibly stale)
 * {@code lastSyncedPosition}, stranding the live position - the CompatMovement landing setback plus a client move
 * back to the synced spot left players a block in the air. Remove once the upstream guard fix ships.
 */
public final class RefreshPositionFix {

    private RefreshPositionFix() {}

    /** A 1-ulp-lifted retry position when the refresh was swallowed (live coords still off target), else null. */
    public static @Nullable Pos swallowedRetry(Entity entity, Pos requested) {
        return entity.getPosition().samePoint(requested) ? null
                : requested.withY(Math.nextUp(requested.y()));
    }
}
