package net.swofty.type.murdermysterygame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

/**
 * Kills players who fall into water with a drowning death message.
 */
public class ActionWaterDeath implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;
        if (game.getState() != GameState.IN_PROGRESS) return;

        // Already eliminated
        if (player.isEliminated()) return;

        // Check block at player's feet position
        Pos pos = player.getPosition();
        Block blockAtFeet = game.getInstanceContainer().getBlock(pos.blockX(), pos.blockY(), pos.blockZ());

        // Check if player is in water
        if (blockAtFeet == Block.WATER) {
            game.onEnvironmentalDeath(player, "You drowned.");
        }
    }
}
