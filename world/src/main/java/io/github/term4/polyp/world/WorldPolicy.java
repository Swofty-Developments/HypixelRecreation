package io.github.term4.polyp.world;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Cross-world gameplay rules, globally pluggable. The default gates everything to the actor's own world;
 * override to open specific reaches (staff hitting/building into an observed game). Views govern what a player
 * SEES; this governs what they DO.
 */
public interface WorldPolicy {

    WorldPolicy SAME_WORLD = new WorldPolicy() {};

    /**
     * Whether {@code actor}'s gameplay effects reach {@code target} (melee, projectiles, splash, pushes, pickup,
     * merging). {@code actor} = the acting ENTITY, not its shooter; an actor may legitimately be world-less.
     */
    default boolean affects(@NotNull Entity actor, @NotNull Entity target) {
        return MechanicsWorld.binding(actor) == MechanicsWorld.binding(target);
    }

    /** Whether {@code viewer}'s client renders {@code subject} - the per-viewer entity filter (view layers re-point it). */
    default boolean sees(@NotNull Player viewer, @NotNull Entity subject) {
        return MechanicsWorld.binding(viewer) == MechanicsWorld.binding(subject);
    }

    /** Whether {@code player} may edit blocks of a world they view without belonging to it (staff build mode). */
    default boolean edits(@NotNull Player player, @NotNull MechanicsWorld viewed) {
        return false;
    }

    /** Whether {@code viewer}'s client renders {@code world}'s BLOCKS - the filter for block-anchored FX. */
    default boolean seesBlocksOf(@NotNull Player viewer, @NotNull MechanicsWorld world) {
        return MechanicsWorld.binding(viewer) == null && viewer.getInstance() == world.instance();
    }

    static boolean canAffect(@NotNull Entity actor, @NotNull Entity target) {
        return Holder.POLICY.affects(actor, target);
    }

    static boolean canSee(@NotNull Player viewer, @NotNull Entity subject) {
        return Holder.POLICY.sees(viewer, subject);
    }

    static boolean canEdit(@NotNull Player player, @NotNull MechanicsWorld viewed) {
        return Holder.POLICY.edits(player, viewed);
    }

    static boolean seesBlocks(@NotNull Player viewer, @NotNull MechanicsWorld world) {
        return Holder.POLICY.seesBlocksOf(viewer, world);
    }

    static @NotNull WorldPolicy get() { return Holder.POLICY; }

    /** Replaces the rules server-wide; returns the previous policy so a wrapper can delegate to it. */
    static @NotNull WorldPolicy set(@NotNull WorldPolicy policy) {
        WorldPolicy previous = Holder.POLICY;
        Holder.POLICY = policy;
        return previous;
    }

    final class Holder {
        private static volatile WorldPolicy POLICY = SAME_WORLD;
        private Holder() {}
    }
}
