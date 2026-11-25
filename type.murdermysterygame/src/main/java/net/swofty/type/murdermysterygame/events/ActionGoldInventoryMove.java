package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionGoldInventoryMove implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(InventoryItemChangeEvent event) {
        if (event.getInventory().getViewers().isEmpty()) return;
        MurderMysteryPlayer player = (MurderMysteryPlayer) event.getInventory().getViewers().stream().findFirst().orElse(null);
        if (player == null) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;
        if (game.getState() != GameState.IN_PROGRESS) return;

        int totalGold = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            if (stack.material() == Material.GOLD_INGOT) {
                totalGold += stack.amount();
                if (i != 8) {
                    player.getInventory().setItemStack(i, ItemStack.AIR);
                }
            }
        }

        if (totalGold > 0) {
            player.getInventory().setItemStack(8, ItemStack.of(Material.GOLD_INGOT, totalGold));
        } else {
            ItemStack slot8Item = player.getInventory().getItemStack(8);
            if (slot8Item.material() == Material.GOLD_INGOT) {
                player.getInventory().setItemStack(8, ItemStack.AIR);
            }
        }
    }
}
