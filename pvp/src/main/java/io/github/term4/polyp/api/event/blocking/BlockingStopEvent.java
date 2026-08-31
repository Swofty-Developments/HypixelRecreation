package io.github.term4.polyp.api.event.blocking;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

/** Fired when a player lowers a blocked item - released, finished, or interrupted (held-slot switch). */
public record BlockingStopEvent(Player player, PlayerHand hand, ItemStack item) implements Event {
    /** The player's gameplay world. */
    public MechanicsWorld world() { return MechanicsWorld.of(player()); }
}
