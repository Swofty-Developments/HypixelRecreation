package net.swofty.type.skywarsgame.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsKitStats;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.game.SkywarsGameStatus;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionMobKill implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (!(entity instanceof LivingEntity mob)) return;

        Entity lastAttacker = mob.getLastDamageSource() != null ?
                mob.getLastDamageSource().getAttacker() : null;

        if (!(lastAttacker instanceof SkywarsPlayer killer)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(killer);
        if (game == null || game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;
        if (killer.isEliminated()) return;

        recordMobKill(killer);
    }

    private void recordMobKill(SkywarsPlayer killer) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(killer);
        if (handler == null) return;

        DatapointSkywarsKitStats kitStatsDP = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class);
        DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
        DatapointSkywarsKitStats.KitStatistics currentKitStats = kitStats.getStatsForKit(killer.getSelectedKit());
        currentKitStats.addMobKilled();
        currentKitStats.addMobKill();
    }
}
