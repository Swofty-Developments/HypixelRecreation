package io.github.term4.polyp.util;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;

/** Held-item helpers shared by the use-item paths. */
public final class HeldItems {

    private HeldItems() {}

    /** Shrinks the held stack by one outside creative (the vanilla use-consume). */
    public static void consumeOne(Player player, PlayerHand hand) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack held = player.getItemInHand(hand);
        player.setItemInHand(hand, held.withAmount(held.amount() - 1));
    }
}
