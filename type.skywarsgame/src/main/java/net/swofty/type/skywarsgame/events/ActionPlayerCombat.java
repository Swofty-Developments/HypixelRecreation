package net.swofty.type.skywarsgame.events;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionPlayerCombat implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof SkywarsPlayer victim)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(victim);
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        if (victim.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamage().getAttacker() instanceof SkywarsPlayer attacker
                && (attacker.isEliminated() || game.getPlayer(attacker.getUuid()).isEmpty())) {
            event.setCancelled(true);
            return;
        }

        float damageAmount = event.getDamage().getAmount();

        if (event.getDamage().getAttacker() instanceof SkywarsPlayer attacker) {
            if (!attacker.equals(victim)) {
                victim.setLastDamager(attacker.getUuid());
            }
        }

        float newHealth = victim.getHealth() - damageAmount;
        if (newHealth <= 0) {
            event.setCancelled(true);
            handleDeath(victim, game, event);
        }
    }

    private void handleDeath(SkywarsPlayer victim, SkywarsGame game, EntityDamageEvent event) {
        if (event.getDamage().getAttacker() instanceof SkywarsPlayer killer) {
            SkywarsGame.KillType killType = SkywarsGame.KillType.MELEE;

            DamageType damageType = event.getDamage().getType().asValue();
            if (damageType == DamageType.ARROW.asValue()) {
                killType = SkywarsGame.KillType.BOW;
            } else if (damageType == DamageType.OUT_OF_WORLD.asValue()) {
                killType = SkywarsGame.KillType.VOID;
            } else if (damageType == DamageType.FALL.asValue()) {
                killType = SkywarsGame.KillType.FALL;
            }

            game.onPlayerKill(killer, victim, killType);
        } else {
            SkywarsGame.EnvironmentalDeathType deathType = SkywarsGame.EnvironmentalDeathType.FALL;

            DamageType damageType = event.getDamage().getType().asValue();
            if (damageType == DamageType.OUT_OF_WORLD.asValue()) {
                deathType = SkywarsGame.EnvironmentalDeathType.VOID;
            } else if (damageType == DamageType.LAVA.asValue()) {
                deathType = SkywarsGame.EnvironmentalDeathType.LAVA;
            } else if (damageType == DamageType.ON_FIRE.asValue() || damageType == DamageType.IN_FIRE.asValue()) {
                deathType = SkywarsGame.EnvironmentalDeathType.FIRE;
            }

            game.onEnvironmentalDeath(victim, deathType);
        }
    }
}
