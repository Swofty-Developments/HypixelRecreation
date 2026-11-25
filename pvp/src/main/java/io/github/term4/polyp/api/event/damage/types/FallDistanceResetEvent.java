package io.github.term4.polyp.api.event.damage.types;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before a gamemode change zeroes the player's accumulated fall distance (vanilla 1.8 behavior; 26.1 dropped it).
 * Cancel to keep the distance - e.g. a mid-air adventure-to-survival switch.
 */
public class FallDistanceResetEvent implements CancellableEvent {

    private final Player player;
    private final GameMode newGameMode;
    private final float distance;
    private boolean cancelled;

    public FallDistanceResetEvent(Player player, GameMode newGameMode, float distance) {
        this.player = player;
        this.newGameMode = newGameMode;
        this.distance = distance;
    }

    public @NotNull Player player() { return player; }
    public @NotNull GameMode newGameMode() { return newGameMode; }

    /** Distance about to be cleared. */
    public float distance() { return distance; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
}
