package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.player.PlayerTickEvent;
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

public class ActionPlayerTick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getState() == GameState.IN_PROGRESS || game.getState() == GameState.ENDING) {
            if (player.getFood() < 20) {
                player.setFood(20);
                player.setFoodSaturation(20.0f);
            }

            ItemStack slot8Item = player.getInventory().getItemStack(8);
            if (slot8Item.material() != Material.GOLD_INGOT) {
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
                }
            }
        }
    }
}
