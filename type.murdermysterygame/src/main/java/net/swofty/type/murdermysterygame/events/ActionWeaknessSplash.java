package net.swofty.type.murdermysterygame.events;

import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.protection.WeaknessProtectionManager;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

/**
 * Detects when the murderer is hit by a weakness splash potion.
 * When this happens, apply the "weakened" state to the murderer.
 */
public class ActionWeaknessSplash implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityPotionAddEvent event) {
        if (!(event.getEntity() instanceof MurderMysteryPlayer player)) return;

        // Only react to weakness effect
        if (event.getPotion().effect() != PotionEffect.WEAKNESS) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null || game.getState() != GameState.IN_PROGRESS) return;

        // Check if this player is the murderer
        GameRole role = game.getRoleManager().getRole(player.getUuid());
        if (role == GameRole.MURDERER) {
            WeaknessProtectionManager.applyWeaknessToMurderer(player);
        }
    }
}
