package net.swofty.type.bedwarsgame.events;

import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.death.BedWarsCombatTracker;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.replay.event.ReplayEntityAnimationEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.ScheduleUtility;

public class ActionGameCombatTrack implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false)
    public void run(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof BedWarsPlayer victim)) {
            return;
        }

        BedWarsGame game = victim.getGame();
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            return;
        }

        if (event.getDamage().getAmount() > 0) {
            game.getReplayManager().getEntityLifecycleDispatcher().recordAnimation(
                    victim.getEntityId(), ReplayEntityAnimationEvent.Animation.TAKE_DAMAGE
            );
            ScheduleUtility.nextTick(() -> game.getReplayManager().recordPlayerHealth(victim));
        }

        Entity source = event.getDamage().getAttacker();
        if (source instanceof ProjectileEntity projectile) {
            source = projectile.getShooter();
        }

        if (source instanceof BedWarsPlayer attacker && event.getDamage().getAmount() > 0) {
            if (!victim.equals(attacker) && !isSameTeam(victim, attacker)) {
                BedWarsCombatTracker.recordAttack(victim, attacker);
            }
        }
    }

    private boolean isSameTeam(BedWarsPlayer first, BedWarsPlayer second) {
        BedWarsMapsConfig.TeamKey team1 = first.getTeamKey();
        BedWarsMapsConfig.TeamKey team2 = second.getTeamKey();
        if (team1 == null || team2 == null) {
            return false;
        }
        return team1.equals(team2);
    }
}
