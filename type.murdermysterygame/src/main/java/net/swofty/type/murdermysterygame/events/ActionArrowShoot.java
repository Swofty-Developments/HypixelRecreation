package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.Material;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

public class ActionArrowShoot implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerCancelItemUseEvent event) {
        if (event.getItemStack().material() != Material.BOW) return;
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;
        if (game.getState() != GameState.IN_PROGRESS) return;
        if (player.isEliminated()) return;

        // Calculate bow power to ensure it's actually a shot
        long duration = event.getUseDuration();
        double power = getPower(duration * 50); // Convert to milliseconds (approx)
        if (power < 0.1) return;

        // Track arrow fired for player
        player.addArrowFired();

        // === ACHIEVEMENT TRACKING ===
        PlayerAchievementHandler achHandler = new PlayerAchievementHandler(player);

        // Per-game: Trigger Happy Havoc - fire 50 arrows in single game
        achHandler.addProgress("murdermystery.trigger_happy", 1);
    }

    private double getPower(long duration) {
        double secs = duration / 1000.0;
        double pow = (secs * secs + secs * 2.0) / 3.0;
        if (pow > 1) {
            pow = 1;
        }
        return pow;
    }
}
