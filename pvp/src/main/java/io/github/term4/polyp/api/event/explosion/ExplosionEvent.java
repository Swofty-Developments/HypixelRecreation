package io.github.term4.polyp.api.event.explosion;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fired after per-entity exposure/falloff is computed, before knockback + damage apply. Cancel to abort. Block
 * destruction is not run here: a listener handles it off {@link #center()} + {@link #power()}.
 */
public class ExplosionEvent implements CancellableEvent {

    /** One entity caught in the explosion: read-only distance/exposure, mutable knockback + damage. */
    public static final class Target {
        private final Entity entity;
        private final double distance;
        private final float exposure;
        private @Nullable Vec knockback;
        private float damage;

        public Target(@NotNull Entity entity, double distance, float exposure, @Nullable Vec knockback, float damage) {
            this.entity = entity;
            this.distance = distance;
            this.exposure = exposure;
            this.knockback = knockback;
            this.damage = damage;
        }

        public @NotNull Entity entity() { return entity; }
        /** Blocks from the explosion center. */
        public double distance() { return distance; }
        /** Raytraced line-of-sight fraction (0..1); 1.0 when exposure is disabled. */
        public float exposure() { return exposure; }
        /** The falloff push, or {@code null} when the entity sits exactly on the center. */
        public @Nullable Vec knockback() { return knockback; }
        public void setKnockback(@Nullable Vec knockback) { this.knockback = knockback; }
        public float damage() { return damage; }
        public void setDamage(float damage) { this.damage = damage; }
    }

    private final MechanicsWorld world;
    private final Point center;
    private final float power;
    private final @Nullable Entity source;
    private final boolean fire;
    private final List<Target> targets;
    private final List<Point> blocks;
    private boolean cancelled;

    public ExplosionEvent(@NotNull MechanicsWorld world, @NotNull Point center, float power, @Nullable Entity source,
                          boolean fire, @NotNull List<Target> targets, @NotNull List<Point> blocks) {
        this.blocks = blocks;
        this.world = world;
        this.center = center;
        this.power = power;
        this.source = source;
        this.fire = fire;
        this.targets = targets;
    }

    public @NotNull Instance instance() { return world.instance(); }
    /** A block-break/fire listener must mutate through this, or a world blast edits the base map. */
    public @NotNull MechanicsWorld world() { return world; }
    public @NotNull Point center() { return center; }
    public float power() { return power; }
    public @Nullable Entity source() { return source; }
    public boolean fire() { return fire; }

    /** Live and mutable: edit or {@code removeIf} to alter the outcome. */
    public @NotNull List<Target> targets() { return targets; }

    /** The blocks this blast will destroy - selected, not yet removed. Live and mutable, empty when it breaks none. */
    public @NotNull List<Point> blocks() { return blocks; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
