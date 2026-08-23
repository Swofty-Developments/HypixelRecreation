package net.swofty.type.ravengarddungeon.events;

import net.minestom.server.event.player.PlayerDeathEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.ravengarddungeon.TypeRavengardDungeonLoader;
import net.swofty.type.ravengarddungeon.user.RavengardDungeonPlayer;

public class ActionPlayerDeath implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerDeathEvent event) {
        event.setChatMessage(null);
        RavengardDungeonPlayer player = (RavengardDungeonPlayer) event.getPlayer();
        String killer = killerName(player.getLastDamageSource());
        ScheduleUtility.nextTick(() -> TypeRavengardDungeonLoader.getGame().eliminate(player, killer));
    }

    private static String killerName(net.minestom.server.entity.damage.Damage damage) {
        if (damage == null) {
            return null;
        }
        net.minestom.server.entity.Entity attacker = damage.getAttacker() != null
                ? damage.getAttacker() : damage.getSource();
        if (attacker instanceof net.swofty.type.ravengardgeneric.entity.mob.RavengardMob mob) {
            return mob.displayName();
        }
        if (attacker instanceof net.minestom.server.entity.Player other) {
            return other.getUsername();
        }
        return null;
    }
}
