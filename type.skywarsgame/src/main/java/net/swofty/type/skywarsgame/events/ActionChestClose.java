package net.swofty.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionChestClose implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        AbstractInventory closedInventory = event.getInventory();
        if (!(closedInventory instanceof Inventory)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getState() != GameState.IN_PROGRESS) return;

        Pos chestPos = game.getChestManager().getChestPositionForInventory((Inventory) closedInventory);
        if (chestPos != null) {
            game.getChestManager().onChestClosed(chestPos);
        }
    }
}
