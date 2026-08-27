package net.swofty.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionChestOpen implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockInteract(PlayerBlockInteractEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        Block block = event.getBlock();
        if (block.compare(Block.CHEST) || block.compare(Block.TRAPPED_CHEST)) {
            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) return;

            if (game.getState() != GameState.IN_PROGRESS) {
                event.setCancelled(true);
                return;
            }

            Pos chestPos = new Pos(event.getBlockPosition());
            if (game.getChestManager().isChestPosition(chestPos)) {
                game.recordChestOpened(player);
                player.openInventory(game.getChestManager().getChestInventory(chestPos));
                event.setCancelled(true);
            }
        }
    }
}
