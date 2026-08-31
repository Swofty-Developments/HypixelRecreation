package net.swofty.type.bedwarsgame.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

public class ActionPlayerInventory implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void changeHeldSlot(PlayerChangeHeldSlotEvent event) {
        BedWarsGame game = TypeBedWarsGameLoader.getGameByInstance(event.getInstance());
        if (game == null || game.getReplayManager() == null) {
            return;
        }
        if (!game.getReplayManager().isRecording()) {
            return;
        }

        var dispatcher = game.getReplayManager().getEntityLifecycleDispatcher();
        if (dispatcher != null) {
            dispatcher.recordHeldItem(event.getPlayer().getEntityId(), event.getItemInNewSlot());
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void changeEquipment(InventoryItemChangeEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) {
            return;
        }

        Player player = event.getInventory().getViewers().stream().findFirst().orElseThrow();
        BedWarsGame game = TypeBedWarsGameLoader.getGameByInstance(player.getInstance());
        if (game == null || game.getReplayManager() == null) {
            return;
        }
        if (!game.getReplayManager().isRecording()) {
            return;
        }

        int slot = event.getSlot();
        if (!(
            slot == PlayerInventoryUtils.HELMET_SLOT ||
            slot == PlayerInventoryUtils.CHESTPLATE_SLOT ||
            slot == PlayerInventoryUtils.LEGGINGS_SLOT ||
            slot == PlayerInventoryUtils.BOOTS_SLOT
            )) return;

        var dispatcher = game.getReplayManager().getEntityLifecycleDispatcher();
        if (dispatcher != null) {
            dispatcher.recordEquipment(
                player.getEntityId(),
                slot,
                event.getNewItem()
            );
        }

    }
}
