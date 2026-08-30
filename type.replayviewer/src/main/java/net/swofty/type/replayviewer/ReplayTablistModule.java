package net.swofty.type.replayviewer;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.type.game.replay.model.ReplayTeam;
import net.swofty.type.generic.tab.CustomTablistSkin;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkin;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class ReplayTablistModule extends TablistModule {
    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(new TablistEntry(
                Text.of("<7>[Viewer] {}", player.getUsername()),
                TablistSkinRegistry.GRAY));

        TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
            session -> {
                addReplayParticipants(entries, session);
            },
                () -> entries.add(new TablistEntry(Text.key("replays.loading"), TablistSkinRegistry.ORANGE))
        );

        return entries;
    }

    private static void addReplayParticipants(List<TablistEntry> entries, ReplaySession session) {
        Map<String, ReplayTeam> teamsById = new HashMap<>();
        Map<UUID, ReplayTeam> teamsByMember = new HashMap<>();
        for (ReplayTeam team : session.getReplayTeams()) {
            teamsById.put(team.id(), team);
            for (UUID member : team.initialMembers()) {
                teamsByMember.put(member, team);
            }
        }

        for (ReplayEntityState state : session.getEntityStore().states().values()) {
            if (state.player() == null) continue;
            UUID participantUuid = state.player().participantUuid();
            ReplayTeam team = teamsById.get(state.player().teamId());
            if (team == null) {
                teamsByMember.remove(participantUuid);
            } else {
                teamsByMember.put(participantUuid, team);
            }
        }

        for (ReplayParticipant participant : session.getMetadata().participants()) {
            ReplayTeam team = teamsByMember.get(participant.uuid());
            TextColor color = team == null ? NamedTextColor.GRAY : TextColor.color(team.color());
            entries.add(new TablistEntry(
                    Text.of("<color:{}>{}", color, participant.username()),
                    getSkin(participant)));
        }
    }

    private static TablistSkin getSkin(ReplayParticipant participant) {
        if (participant.textureValue() == null || participant.textureValue().isEmpty()) {
            return TablistSkinRegistry.GRAY;
        }
        return new CustomTablistSkin(participant.textureValue(), participant.textureSignature());
    }
}
