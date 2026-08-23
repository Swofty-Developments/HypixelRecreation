package net.swofty.type.murdermysterygame.maphandler;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.game.GameStatus;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.util.List;

public abstract class MapHandler {

    /**
     * Returns the map ID this handler is for (e.g., "aquarium")
     */
    public abstract String getMapId();

    /**
     * Returns list of interactive elements for this map
     */
    public abstract List<InteractiveElement> getInteractiveElements();

    /**
     * Called when the game starts on this map
     */
    public void onGameStart(Game game) {
        // Default: no-op, override if needed
    }

    /**
     * Called when the game ends on this map
     */
    public void onGameEnd(Game game) {
        // Default: no-op, override if needed
    }

    /**
     * Handle a block interaction at a position
     * @return true if interaction was handled, false otherwise
     */
    public final boolean handleInteraction(PlayerBlockInteractEvent event,
                                           MurderMysteryPlayer player,
                                           Game game) {
        Point blockPos = event.getBlockPosition();

        for (InteractiveElement element : getInteractiveElements()) {
            if (element.matchesPosition(blockPos)) {
                return processInteraction(element, event, player, game);
            }
        }
        return false;
    }

    /**
     * Process the interaction - validates gold and executes action
     */
    private boolean processInteraction(InteractiveElement element,
                                        PlayerBlockInteractEvent event,
                                        MurderMysteryPlayer player,
                                        Game game) {
        int goldRequired = element.goldCost();

        // If gold cost is 0, skip validation (element handles its own logic)
        if (goldRequired > 0) {
            int playerGold = game.getGoldManager().countGoldInInventory(player);

            if (playerGold < goldRequired) {
                player.sendMessage("<c>This action requires {} gold", goldRequired);
                return true;
            }

            game.getGoldManager().removeGoldFromInventory(player, goldRequired);
        }

        element.action().execute(event, player, game, this);
        return true;
    }

    /**
     * Utility method for subclasses to regenerate blocks after delay
     */
    protected void scheduleBlockRegeneration(Game game, Point position,
                                             Block originalBlock,
                                             int delaySeconds) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (game.getGameStatus() == GameStatus.IN_PROGRESS) {
                game.getInstanceContainer().setBlock(position, originalBlock);
            }
        }).delay(TaskSchedule.seconds(delaySeconds)).schedule();
    }
}
