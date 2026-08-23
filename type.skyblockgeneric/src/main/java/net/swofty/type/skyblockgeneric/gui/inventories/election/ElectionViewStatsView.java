package net.swofty.type.skyblockgeneric.gui.inventories.election;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.ElectionData;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElectionViewStatsView extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
                (s, ctx) -> Text.key("gui_election.stats.title", SkyBlockCalendar.getYear()),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        ElectionData data = ElectionManager.getElectionData();

        if (!data.isElectionOpen() || data.getCandidates().isEmpty()) {
            layout.slot(22, (s, c) -> ItemStacks.item(Material.BARRIER, 1, Text.key("gui_election.view.no_election"),
                    Text.keyLines("gui_election.view.no_election.lore")));
            return;
        }

        Map<String, Long> tally = data.tallyVotes();
        long totalVotes = tally.values().stream().mapToLong(Long::longValue).sum();
        int currentYear = SkyBlockCalendar.getYear();

        List<ElectionData.CandidateData> candidates = data.getCandidates();

        String leaderName = null;
        long leaderVotes = -1;
        for (ElectionData.CandidateData c : candidates) {
            long v = tally.getOrDefault(c.getMayorName(), 0L);
            if (v > leaderVotes) {
                leaderVotes = v;
                leaderName = c.getMayorName();
            }
        }

        int candidateCount = Math.min(candidates.size(), 6);
        int[] cols = candidateCount <= 5
                ? new int[]{0, 2, 4, 6, 8}
                : new int[]{0, 1, 3, 5, 7, 8};

        for (int i = 0; i < candidateCount; i++) {
            ElectionData.CandidateData candidate = candidates.get(i);
            SkyBlockMayor mayor = candidate.getMayorEnum();
            if (mayor == null) continue;

            long votes = tally.getOrDefault(candidate.getMayorName(), 0L);
            String voteStr = formatVotes(votes);
            String pctStr = totalVotes > 0
                ? String.format("%.1f%%", (votes * 100.0) / totalVotes) : "0%";
            int yearsSince = data.getYearsSinceLastElected(candidate.getMayorName(), currentYear);
            String candidateName = candidate.getMayorName();
            boolean isLeader = candidateName.equals(leaderName) && votes > 0;
            Material glassMaterial = isLeader ? Material.ORANGE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;

            int col = cols[i];

            for (int row = 0; row < 6; row++) {
                int slot = row * 9 + col;

                int finalRow = row;
                layout.slot(slot, (s, c) -> {
                    String playerVote = ElectionManager.getPlayerVote(c.player().getUuid());
                    boolean isVotedFor = candidateName.equals(playerVote);

                    List<Text> lore = buildCandidateLore(
                        mayor, candidate, data.getElectionYear(),
                        yearsSince, voteStr, pctStr, isVotedFor, isLeader
                    );

                    if (finalRow == 5) {
                        return ItemStacks.head(
                            new PlayerSkin(mayor.getTexture(), mayor.getSignature()),
                            candidate.getColoredName(),
                            lore
                        );
                    }
                    return ItemStacks.item(
                        glassMaterial, 1,
                        candidate.getColoredName(),
                        lore
                    );
                }, (_, c) -> {
                    String playerVote = ElectionManager.getPlayerVote(c.player().getUuid());
                    if (candidateName.equals(playerVote)) {
                        c.player().sendMessage(Text.key("gui_election.view.already_voted"));
                        return;
                    }
                    ElectionManager.castVote(c.player().getUuid(), candidateName);
                    c.player().sendMessage(Text.key("gui_election.view.vote_divider"));
                    c.player().sendMessage(Text.key("gui_election.view.vote_cast", candidate.getColoredName(), data.getElectionYear()));
                    c.player().sendMessage(Text.literal("  ").append(Text.key("gui_election.view.vote_fame")));
                    c.player().sendMessage(Text.key("gui_election.view.vote_result", candidate.getColoredName(), pctStr, voteStr));
                    c.player().sendMessage(Text.key("gui_election.view.vote_divider"));
                    c.replace(new ElectionViewStatsView());
                });
            }
        }
    }

    private List<Text> buildCandidateLore(SkyBlockMayor mayor, ElectionData.CandidateData candidate,
                                            int electionYear, int yearsSince,
                                            String voteStr, String pctStr,
                                            boolean votedFor, boolean isLeader) {
        Text color = Text.of("<color:{}>", candidate.getColor());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key("gui_election.view.candidate.year", electionYear));
        lore.add(Text.empty());
        lore.add(Text.key("gui_election.view.candidate.votes", color, voteStr, pctStr));
        if (isLeader) {
            lore.add(Text.key("gui_election.stats.leader", color));
        }
        if (yearsSince >= 0) {
            lore.add(Text.key("gui_election.view.candidate.last_elected", color, yearsSince));
        } else {
            lore.add(Text.key("gui_election.view.candidate.last_elected_never", color));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<8><m>--------------------------"));

        List<SkyBlockMayor.Perk> activePerks = candidate.getActivePerkEnums();
        for (int j = 0; j < activePerks.size(); j++) {
            SkyBlockMayor.Perk perk = activePerks.get(j);
            if (j == 0) {
                lore.add(Text.of("<6>✯ ").append("<color:{0}>{1}", candidate.getColor(), perk.getDisplayName()));
            } else {
                lore.addAll(Text.of("<color:{0}><wrap:35>{1}</wrap>", candidate.getColor(), perk.getDisplayName()).lines());
            }
            lore.addAll(Text.of("<wrap:35>{}</wrap>", perk.getDescription()).lines());
            if (j < activePerks.size() - 1) lore.add(Text.empty());
        }

        lore.add(Text.of("<8><m>--------------------------"));

        if (!mayor.isSpecial()) {
            lore.add(Text.empty());
            lore.add(Text.key("gui_election.view.candidate.minister_note_1", color));
            lore.add(Text.key("gui_election.view.candidate.minister_note_2"));
        }

        lore.add(Text.empty());
        if (votedFor) {
            lore.add(Text.key("gui_election.view.candidate.voted"));
        } else {
            lore.add(Text.key("gui_election.view.candidate.click_vote", mayor.getDisplayName()));
        }

        return lore;
    }

    private String formatVotes(long votes) {
        if (votes >= 1_000_000) return String.format("%.1fM", votes / 1_000_000.0);
        if (votes >= 1_000) return String.format("%.1fk", votes / 1_000.0);
        return String.valueOf(votes);
    }
}
