package net.swofty.type.generic.event.actions.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.resourcepack.ResourcePackManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.flow.PlayerFlow;

public class ActionPlayerDataLoad implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, phase = EventPhase.LOAD_DATA)
    public void run(AsyncPlayerConfigurationEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ServerType type = HypixelConst.getTypeLoader().getType();
        PlayerFlow.run(player, "data/load", () -> {
            PlayerDataService.loadAll(type, player.getUuid());
            PlayerDataService.attachAll(type, player);
        });

        ResourcePackManager packManager = HypixelConst.getResourcePackManager();
        if (packManager != null) {
            PlayerFlow.run(player, "data/resource-pack", () -> packManager.sendPackBlocking(player, 25));
        }
    }
}
