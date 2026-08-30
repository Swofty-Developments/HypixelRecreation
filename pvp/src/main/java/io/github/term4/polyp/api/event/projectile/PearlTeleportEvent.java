package io.github.term4.polyp.api.event.projectile;

import io.github.term4.polyp.mechanics.projectile.entities.PearlEntity;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before a pearl moves its shooter, with the preset-resolved target. Cancel to consume the pearl without
 * moving anyone - the arena-boundary rule (MineMen region-cancels out-of-bounds pearls); {@link #setTarget}
 * redirects instead.
 */
public class PearlTeleportEvent implements CancellableEvent {

    private final PearlEntity pearl;
    private final Entity shooter;
    private Pos target;
    private boolean cancelled;

    public PearlTeleportEvent(PearlEntity pearl, Entity shooter, Pos target) {
        this.pearl = pearl;
        this.shooter = shooter;
        this.target = target;
    }

    public @NotNull PearlEntity pearl() { return pearl; }
    public @NotNull Entity shooter() { return shooter; }

    public @NotNull Pos target() { return target; }
    public void setTarget(@NotNull Pos target) { this.target = target; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
