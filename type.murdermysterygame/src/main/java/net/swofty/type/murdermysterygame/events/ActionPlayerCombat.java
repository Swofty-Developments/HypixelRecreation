package net.swofty.type.murdermysterygame.events;

import io.github.term4.polyp.api.event.attack.PreAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;
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

public class ActionPlayerCombat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PreAttackEvent event) {
        if (!(event.attacker() instanceof MurderMysteryPlayer attacker)) return;
        if (!(event.target() instanceof MurderMysteryPlayer victim)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(attacker);
        if (game == null) {
            event.setCancelled(true);
            return;
        }

        // No combat in waiting lobby
        if (game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        // Check if victim is already eliminated
        if (victim.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        // Check if attacker is eliminated (spectator)
        if (attacker.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        GameRole attackerRole = game.getRoleManager().getRole(attacker.getUuid());
        if (attackerRole == null) {
            event.setCancelled(true);
            return;
        }

        // Only murderer with knife can melee kill
        if (attackerRole == GameRole.MURDERER) {
            // Check if holding knife (iron sword) and murderer has received their sword
            Material heldItem = attacker.getItemInMainHand().material();
            if (heldItem == Material.IRON_SWORD && game.hasMurdererReceivedSword()) {
                // Check if murderer is weakened (from weakness splash potion)
                if (!WeaknessProtectionManager.handleMurdererAttack(attacker)) {
                    // Murderer is weakened - attack absorbed
                    attacker.sendMessage(Component.text("Your attack was weakened!",
                            NamedTextColor.YELLOW));
                    event.setCancelled(true);
                    return;
                }
                // Kill the victim
                game.onPlayerKill(attacker, victim);
            }
            // Cancel the attack either way - murderer either kills instantly or does nothing
            event.setCancelled(true);
        } else {
            // Innocents and detectives cannot melee kill - they use bow
            // Cancel regular melee attacks
            event.setCancelled(true);
        }
    }
}
