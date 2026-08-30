package net.swofty.type.murdermysterygame.events;

import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.game.GameStatus;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

public class ActionArrowHit implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ProjectileCollideWithEntityEvent event) {
        Entity projectile = event.getEntity();
        Entity target = event.getTarget();

        if (!(projectile instanceof ProjectileEntity arrow)) return;
        if (!(target instanceof MurderMysteryPlayer victim)) return;

        Entity shooter = arrow.getShooter();
        if (!(shooter instanceof MurderMysteryPlayer attacker)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(attacker);
        if (game == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        if (victim.isEliminated() || attacker.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        // Handle self-shot - Elementary achievement
        if (attacker.getUuid().equals(victim.getUuid())) {
            PlayerAchievementHandler achHandler = new PlayerAchievementHandler(attacker);
            achHandler.addProgress("murdermystery.elementary", 1);
            // Kill the player who shot themselves
            game.onEnvironmentalDeath(attacker);
            event.setCancelled(true);
            return;
        }

        // Calculate distance for Sharpshooter achievement
        double distance = attacker.getPosition().distance(victim.getPosition());

        GameRole attackerRole = game.getRoleManager().getRole(attacker.getUuid());
        GameRole victimRole = game.getRoleManager().getRole(victim.getUuid());

        // === BOW ACHIEVEMENT TRACKING ===
        PlayerAchievementHandler achHandler = new PlayerAchievementHandler(attacker);

        // Track bow kills for streak
        attacker.addBowKill();

        // Murderer bow achievements
        if (attackerRole == GameRole.MURDERER) {
            // Track murderer bow kills this game
            attacker.setMurdererBowKillsThisGame(attacker.getMurdererBowKillsThisGame() + 1);

            // Per-game: Wrong Weapon - Kill 3 people with Bow as Murderer
            achHandler.addProgress("murdermystery.wrong_weapon", 1);

            // Humiliation - Kill Detective as Murderer with Bow
            if (victimRole == GameRole.DETECTIVE) {
                achHandler.addProgress("murdermystery.humiliation", 1);
            }
        }

        // Killing the Murderer achievements
        if (victimRole == GameRole.MURDERER) {
            // Sharpshooter - Kill Murderer as Detective from 30+ blocks
            if (attackerRole == GameRole.DETECTIVE && distance >= 30.0) {
                achHandler.addProgress("murdermystery.sharpshooter", 1);
            }

            // On Point - Kill Murderer as Innocent with first Arrow
            if (attackerRole == GameRole.INNOCENT && attacker.getArrowsFiredThisGame() == 1) {
                achHandler.addProgress("murdermystery.on_point", 1);
            }
        }

        // The Arrow Streak - Kill 5 players in a row with Bow
        if (attacker.getConsecutiveBowKills() >= 5) {
            achHandler.addProgress("murdermystery.arrow_streak", 1);
        }

        // Process the kill (bow kill)
        game.onPlayerKill(attacker, victim, Game.KillType.BOW);
    }
}
