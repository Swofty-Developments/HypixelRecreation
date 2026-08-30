package io.github.term4.polyp.api.event.projectile;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ResolvedFlight;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired after a projectile entity is built and configured but before it enters the world. End of flight is
 * {@link ProjectileHitEvent}.
 */
public class ProjectileLaunchEvent implements CancellableEvent {

    private final ProjectileSnapshot snapshot;
    private final ProjectileEntity projectile;
    private final ResolvedFlight resolvedFlight;
    private Pos spawnPos;
    private Vec velocity;
    private @Nullable ProjectileBehavior behavior;
    private boolean cancelled;

    public ProjectileLaunchEvent(ProjectileSnapshot snapshot, ProjectileEntity projectile, ResolvedFlight resolvedFlight, Pos spawnPos, Vec velocity) {
        this.snapshot = snapshot;
        this.projectile = projectile;
        this.resolvedFlight = resolvedFlight;
        this.spawnPos = spawnPos;
        this.velocity = velocity;
    }

    public ProjectileSnapshot snapshot() { return snapshot; }
    public @Nullable Entity shooter() { return snapshot.shooter(); }

    /** Flight knobs the entity was already stamped with; redirect via {@link #setSpawnPos}/{@link #setVelocity}. */
    public ResolvedFlight resolvedFlight() { return resolvedFlight; }

    /** The built entity, not yet in the world. */
    public @NotNull ProjectileEntity projectile() { return projectile; }
    /** The shooter's world - the one the projectile launches into. */
    public MechanicsWorld world() { return MechanicsWorld.of(snapshot.shooter()); }

    public @NotNull Pos spawnPos() { return spawnPos; }
    public void setSpawnPos(@NotNull Pos pos) { this.spawnPos = pos; }

    /** Initial velocity in blocks/tick. */
    public @NotNull Vec velocity() { return velocity; }
    public void setVelocity(@NotNull Vec velocityBt) { this.velocity = velocityBt; }

    /** Per-launch override ({@code null} = keep the config/snapshot behavior). */
    public @Nullable ProjectileBehavior behavior() { return behavior; }
    public void behavior(@Nullable ProjectileBehavior behavior) { this.behavior = behavior; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
