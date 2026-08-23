package net.swofty.type.generic.event.actions.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.proxy.to.FinishedWithPlayerProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.flow.PlayerFlow;
import net.swofty.type.generic.utility.MathUtility;

public class ActionPlayerDataSave implements HypixelEventClass {
    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true, phase = EventPhase.PERSIST)
    public void run(PlayerDisconnectEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ServerType type = HypixelConst.getTypeLoader().getType();
        PlayerFlow.run(player, "data/save", () -> {
            PlayerDataService.saveAndUnloadAll(type, player);
            RedisClient.requestProxy(new FinishedWithPlayerProtocol(),
                    new FinishedWithPlayerProtocol.Request(player.getUuid().toString()));
            MathUtility.delay(() -> HypixelConst.getTypeLoader().getTablistManager().deleteTablistEntries(player), 5);
        });
    }
}
