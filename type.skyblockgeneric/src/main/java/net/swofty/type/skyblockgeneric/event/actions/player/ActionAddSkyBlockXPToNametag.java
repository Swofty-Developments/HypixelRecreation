package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.swofty.commons.StringUtility;
import net.swofty.commons.TeamColorUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionAddSkyBlockXPToNametag implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!event.isFirstSpawn()) return;

        ScheduleUtility.delay(() -> {
            updatePlayerNametag(player);
        }, 15);
    }

    public static void updatePlayerNametag(SkyBlockPlayer player) {
        if (!player.isOnline()) return;

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        Rank rank = player.getRank();

        String teamName = StringUtility.limitStringLength(rank.getPriorityCharacter() + player.getUsername(), 15);
        Text levelPrefix = Text.of("<8>[")
                .append(Text.parse(experience.getLevel().getColor() + experience.getLevel()))
                .append("<8>] ");
        Team team = new TeamBuilder("Z" + teamName, MinecraftServer.getTeamManager())
            .prefix(levelPrefix.asComponent().append(player.getRankPrefix()))
                .teamColor(TeamColorUtil.fromNamedColor(rank.getTextColor()))
                .build();
        player.setTeam(team);
        player.getTeam().sendUpdatePacket();
    }
}
