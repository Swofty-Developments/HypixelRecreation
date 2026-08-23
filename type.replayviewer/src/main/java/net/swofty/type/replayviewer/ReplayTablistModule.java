package net.swofty.type.replayviewer;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.tab.CustomTablistSkin;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkin;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.playback.bedwars.BedWarsViewerMetadata;

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
        if (!(session.getGameMetadata() instanceof BedWarsViewerMetadata bedWars)) {
            return;
        }

        Map<UUID, BedWarsViewerMetadata.Team> teamsByMember = new HashMap<>();
        for (BedWarsViewerMetadata.Team team : bedWars.teams()) {
            List<UUID> members = session.getCurrentTeams().getOrDefault(team.id(), team.initialMembers());
            for (UUID member : members) {
                teamsByMember.put(member, team);
            }
        }

        for (ReplayParticipant participant : session.getMetadata().participants()) {
            BedWarsViewerMetadata.Team team = teamsByMember.get(participant.uuid());
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
