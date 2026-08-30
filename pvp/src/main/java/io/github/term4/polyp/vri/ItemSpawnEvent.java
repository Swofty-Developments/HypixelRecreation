package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

/**
 * Fired before a VRI drop enters the world ({@link DroppedItemEntity#spawn}). The entity is live - adjust its
 * stack, pickup delay or velocity here. Cancel to drop nothing.
 */
public final class ItemSpawnEvent implements CancellableEvent {

    public enum Cause { BLOCK_DROP, PLAYER_DROP, SERVER }

    private final DroppedItemEntity item;
    private final Cause cause;
    private final @Nullable Player player;
    private final MechanicsWorld world;
    private final Pos position;
    private boolean cancelled;

    public ItemSpawnEvent(DroppedItemEntity item, Cause cause, @Nullable Player player, MechanicsWorld world, Pos position) {
        this.item = item;
        this.cause = cause;
        this.player = player;
        this.world = world;
        this.position = position;
    }

    public DroppedItemEntity item() { return item; }
    public Cause cause() { return cause; }
    /** The breaker/dropper, or {@code null} for a {@link Cause#SERVER} spawn. */
    public @Nullable Player player() { return player; }
    public MechanicsWorld world() { return world; }
    public Instance instance() { return world.instance(); }
    public Pos position() { return position; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
