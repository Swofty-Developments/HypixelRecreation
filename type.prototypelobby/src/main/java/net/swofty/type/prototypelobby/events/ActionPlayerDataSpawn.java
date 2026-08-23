package net.swofty.type.prototypelobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

public class ActionPlayerDataSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, isAsync = true, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;

        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        Rank rank = player.getRank();
        if (rank == Rank.DEFAULT) return;

        Text joinMessage = Text.of("{} <6>joined the lobby!", player.getFullDisplayName());
        for (HypixelPlayer onlinePlayer : HypixelGenericLoader.getLoadedPlayers()) {
            onlinePlayer.sendMessage(joinMessage);
        }

        player.sendMessage("");
        player.sendMessage("<f>➔ <6><l>Welcome to the Prototype Lobby");
        player.sendMessage("<a>All games in this lobby are currently in development.");
        player.sendMessage("<click:url:'https://hypixel.net/PTL'><e>Click here to leave feedback! <f>➤ <b><n>https://hypixel.net/PTL");
        player.sendMessage("");
    }
}
