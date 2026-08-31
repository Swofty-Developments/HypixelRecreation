package io.github.term4.polyp.api.event.projectile;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ResolvedHit;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig.HitResponse;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig.KnockbackSource;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a projectile hits an entity or block, before effects apply: a {@link #snapshot()}, a {@link #resolvedHit()}
 * preview, and per-hit overrides (each unset = use resolved). Cancel suppresses everything and the projectile keeps
 * flying.
 */
public class ProjectileHitEvent implements CancellableEvent {

    private final ProjectileEntity projectile;
    private final ProjectileSnapshot snapshot;
    private final @Nullable Entity shooter;
    private final @Nullable Entity target;
    private final Point hitPoint;
    private final @Nullable Pos throwOrigin;
    private final ResolvedHit resolved;
    private final double resolvedDamage;

    private @Nullable Double damage;
    private @Nullable KnockbackConfig knockback;
    private @Nullable KnockbackSource knockbackSource;
    private @Nullable Boolean removeOnHit;
    private @Nullable HitResponse response;

    private boolean cancelled;

    public ProjectileHitEvent(ProjectileEntity projectile, ProjectileSnapshot snapshot, @Nullable Entity shooter,
                              @Nullable Entity target, Point hitPoint, @Nullable Pos throwOrigin,
                              ResolvedHit resolved, double resolvedDamage) {
        this.projectile = projectile;
        this.snapshot = snapshot;
        this.shooter = shooter;
        this.target = target;
        this.hitPoint = hitPoint;
        this.throwOrigin = throwOrigin;
        this.resolved = resolved;
        this.resolvedDamage = resolvedDamage;
    }

    public @NotNull ProjectileEntity projectile() { return projectile; }
    public ProjectileSnapshot snapshot() { return snapshot; }
    public @Nullable Entity shooter() { return shooter; }
    /** The hit entity, or {@code null} for a block hit. */
    public @Nullable Entity target() { return target; }
    /** The projectile's world. */
    public MechanicsWorld world() { return MechanicsWorld.of(projectile()); }
    public @NotNull Point hitPoint() { return hitPoint; }
    public boolean isBlockHit() { return target == null; }
    public boolean isSelfHit() { return target != null && target == shooter; }
    /** Shooter pos + view at throw time, or {@code null}. */
    public @Nullable Pos throwOrigin() { return throwOrigin; }

    /** Config preview, before overrides. */
    public ResolvedHit resolvedHit() { return resolved; }

    /** The {@link #damage(double) override} else the resolved amount. {@code 0} for a block hit. */
    public double damage() { return damage != null ? damage : resolvedDamage; }
    public void damage(double amount) { this.damage = amount; }

    /** Knockback on a landing hit: override else resolved ({@code null} = none). */
    public @Nullable KnockbackConfig knockback() { return knockback != null ? knockback : resolved.knockback(); }
    public void knockback(@Nullable KnockbackConfig kb) { this.knockback = kb; }

    /** Knockback origin frame: override else resolved. */
    public KnockbackSource knockbackSource() { return knockbackSource != null ? knockbackSource : resolved.knockbackSource(); }
    public void knockbackSource(KnockbackSource source) { this.knockbackSource = source; }

    /** Override else the resolved entity/block-hit flag. */
    public boolean removeOnHit() {
        if (removeOnHit != null) return removeOnHit;
        return isBlockHit() ? resolved.removeOnBlockHit() : resolved.removeOnEntityHit();
    }
    public void removeOnHit(boolean remove) { this.removeOnHit = remove; }

    /** Forced outcome, or {@code null} to use the config's self-hit / invuln-hit logic. */
    public @Nullable HitResponse response() { return response; }
    public void response(@Nullable HitResponse response) { this.response = response; }

    public void cancel() { setCancelled(true); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
