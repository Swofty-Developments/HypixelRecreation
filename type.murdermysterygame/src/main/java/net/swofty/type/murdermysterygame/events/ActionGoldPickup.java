package net.swofty.type.murdermysterygame.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.Material;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.game.GameStatus;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionGoldPickup implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PickupItemEvent event) {
        if (!(event.getLivingEntity() instanceof MurderMysteryPlayer player)) return;

        Entity itemEntity = event.getItemEntity();
        if (itemEntity.getEntityType() != EntityType.ITEM) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        ItemEntityMeta meta = (ItemEntityMeta) itemEntity.getEntityMeta();
        Material material = meta.getItem().material();

        // Handle bow pickup (dropped detective bow)
        if (material == Material.BOW) {
            event.setCancelled(true);

            // Check if this is the dropped detective bow
            if (game.isDroppedDetectiveBow(itemEntity)) {
                // Murderer cannot pick up the bow
                GameRole playerRole = game.getRoleManager().getRole(player.getUuid());
                if (playerRole == GameRole.MURDERER) {
                    // Silently prevent pickup - murderer cannot pick up the bow
                    return;
                }

                // Non-murderer picks up the bow
                game.onDetectiveBowPickedUp(player);
            }
            return;
        }

        // Handle gold pickup
        if (material == Material.GOLD_INGOT) {
            // Check if this is spawned gold (managed by GoldManager)
            if (game.getGoldManager().getSpawnedGold().contains(itemEntity)) {
                // Cancel default pickup, use our collection system
                event.setCancelled(true);
                game.getGoldManager().collectGold(player, itemEntity);
                player.sendMessage("<6>+1 Gold");
                return;
            }

            // This is dropped gold from another player - allow normal pickup
            // Track achievement for picked up gold
            int amount = meta.getItem().amount();
            player.addGoldCollectedThisGame(amount);
            player.sendMessage("<6>+{} Gold", amount);

            // Check if player now has enough gold for bow
            game.getGoldManager().checkPlayerGoldForBow(player);
        }
    }
}
