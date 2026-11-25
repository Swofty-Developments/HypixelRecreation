package io.github.term4.polyp.tracking.motion;

import io.github.term4.polyp.tracking.SprintTracker;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Input a {@link VelocityRule} composes - bundles the entity and the {@link MotionTracker} reads (air-time, launch,
 * position-delta, ground state) so a rule written outside the library has the same primitives the built-ins use.
 */
public final class VelocityContext {

    private final Entity entity;
    private final @Nullable SprintTracker sprintTracker;

    private VelocityContext(Entity entity, @Nullable SprintTracker sprintTracker) {
        this.entity = entity;
        this.sprintTracker = sprintTracker;
    }

    /** Sprint reads degrade to the server-side {@link Entity#isSprinting()}. */
    public static VelocityContext of(Entity entity) {
        return new VelocityContext(entity, null);
    }

    public static VelocityContext of(Entity entity, @Nullable SprintTracker sprintTracker) {
        return new VelocityContext(entity, sprintTracker);
    }

    public Entity entity() { return entity; }

    public @Nullable SprintTracker sprintTracker() { return sprintTracker; }

    /** Server-side sprint state. */
    public boolean wasRecentlySprinting(long ticks) {
        return SprintTracker.wasRecentlySprinting(sprintTracker, entity, ticks);
    }

    /** The <em>client</em>'s sprint state. */
    public boolean wasClientRecentlySprinting(int ticks) {
        return SprintTracker.wasClientRecentlySprinting(sprintTracker, entity, ticks);
    }

    public Vec positionDelta() { return MotionTracker.positionDelta(entity); }

    public int ticksInAir() { return MotionTracker.ticksInAir(entity); }

    /** Left the ground rising (jump or knockback boost) and still in that launch arc. */
    public boolean launched() { return MotionTracker.launched(entity); }

    /** {@code ticks} of fall prediction; see {@link MotionTracker#onGround(Entity, int)}. */
    public boolean onGround(int ticks) { return MotionTracker.onGround(entity, ticks); }

    public @Nullable MotionTracker.JumpInfo recentJump() { return MotionTracker.recentJump(entity); }

    public Vec entityPush() { return MotionTracker.entityPush(entity); }
}
