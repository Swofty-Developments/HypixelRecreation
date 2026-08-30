package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable input describing a single projectile launch: the producing {@link ProjectileType} or {@code Shootable}
 * builds it and hands it to {@link ProjectileSystem#launch}.
 *
 * @param shooter  knockback origin, damage source, hit immunity
 * @param item     the item that launched it (snowball stack, bow), for {@code ctx.item()} config lambdas
 * @param power    throw strength / bow draw ({@code 1.0} baseline) - scales the resolved speed
 * @param spawnPos override, or {@code null} to compute from the shooter's eye + offset
 * @param velocity override in b/t, or {@code null} to compute from aim + speed + spread
 * @param config   override, or {@code null} to resolve the scope chain (profile -&gt; install)
 * @param behavior override, or {@code null} to use the type config's {@code behavior} knob
 */
public record ProjectileSnapshot(Entity shooter, ProjectileType type, @Nullable ItemStack item, double power,
                                 @Nullable Pos spawnPos, @Nullable Vec velocity, @Nullable ProjectileConfig config,
                                 @Nullable ProjectileBehavior behavior) {

    public static ProjectileSnapshot of(Entity shooter, ProjectileType type) {
        return new ProjectileSnapshot(shooter, type, null, 1.0, null, null, null, null);
    }

    public ProjectileSnapshot withItem(@Nullable ItemStack i) { return new ProjectileSnapshot(shooter, type, i, power, spawnPos, velocity, config, behavior); }
    public ProjectileSnapshot withPower(double p) { return new ProjectileSnapshot(shooter, type, item, p, spawnPos, velocity, config, behavior); }
    public ProjectileSnapshot withSpawnPos(@Nullable Pos p) { return new ProjectileSnapshot(shooter, type, item, power, p, velocity, config, behavior); }
    public ProjectileSnapshot withVelocity(@Nullable Vec v) { return new ProjectileSnapshot(shooter, type, item, power, spawnPos, v, config, behavior); }
    public ProjectileSnapshot withConfig(@Nullable ProjectileConfig c) { return new ProjectileSnapshot(shooter, type, item, power, spawnPos, velocity, c, behavior); }
    public ProjectileSnapshot withBehavior(@Nullable ProjectileBehavior b) { return new ProjectileSnapshot(shooter, type, item, power, spawnPos, velocity, config, b); }
}
