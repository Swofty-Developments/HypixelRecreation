package net.swofty.type.murdermysterygame.events;

import io.github.term4.polyp.api.event.damage.PreDamageEvent;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import net.minestom.server.entity.GameMode;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

public class ActionPreventFallDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PreDamageEvent event) {
        if (!(event.target() instanceof MurderMysteryPlayer player)) return;

        // Allow damage in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        // Check if this is fall damage
        if (event.type().key().equals(FallDamage.KEY)) {
            event.setCancelled(true);
        }
    }
}
