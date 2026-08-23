package net.swofty.type.ravengarddungeon.events;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.commons.ServerType;
import net.swofty.type.game.game.Game;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.ravengarddungeon.TypeRavengardDungeonLoader;
import net.swofty.type.ravengarddungeon.user.RavengardDungeonPlayer;

public class ActionPlayerJoin implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, phase = EventPhase.GAMEPLAY)
    public void run(AsyncPlayerConfigurationEvent event) {
        RavengardDungeonPlayer player = (RavengardDungeonPlayer) event.getPlayer();
        ScheduleUtility.delay(() -> {
            if (!player.isOnline()) return;
            for (var instance : net.swofty.type.ravengarddungeon.game.DungeonInstanceRegistry.all()) {
                if (instance.getPlayers().contains(player.getUuid())) {
                    return;
                }
            }
            Game.JoinResult result = TypeRavengardDungeonLoader.getGame().join(player);
            if (result instanceof Game.JoinResult.Denied(String reason)) {
                player.sendMessage("<c>Could not enter the dungeon: {}", reason);
                player.sendTo(ServerType.RAVENGARD_LOBBY);
            }
        }, 20);
    }
}
