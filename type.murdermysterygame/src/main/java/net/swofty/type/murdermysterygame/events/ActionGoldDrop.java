package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.Material;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionGoldDrop implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ItemDropEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        Material material = event.getItemStack().material();

        if (material == Material.GOLD_INGOT) {
            event.setCancelled(true);
            return;
        }

        // Prevent dropping weapons
        if (material == Material.BOW || material == Material.ARROW || material == Material.IRON_SWORD) {
            event.setCancelled(true);
        }
    }
}
