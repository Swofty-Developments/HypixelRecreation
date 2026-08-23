package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.scoreboard.HypixelScoreboard;
import net.swofty.type.generic.utility.BlockUtility;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class SkyBlockScoreboard {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final Text BLANK = Text.literal(" ");
    private static final HypixelScoreboard scoreboard = new HypixelScoreboard();
    private static Integer skyblockName = 0;

    public static void start() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        scheduler.submitTask(() -> {
            skyblockName++;
            if (skyblockName > 50) {
                skyblockName = 0;
            }

            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                Locale l = player.getLocale();
                SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
                SkyBlockRegion region = player.getRegion();
                MissionData missionData = player.getMissionData();

                if (dataHandler == null) {
                    continue;
                }

                TextBody body = new TextBody();
                body.section("date").line(Text.key("scoreboard.common.date_line",
                    DATE_FORMAT.format(LocalDateTime.now(ZoneId.systemDefault())),
                    HypixelConst.getServerName()));

                TextBody.Section location = body.section("location");
                location.line(BLANK);
                location.line(Text.key("scoreboard.skyblock.calendar_date_line",
                    SkyBlockCalendar.getMonthName(),
                    StringUtility.ntify(SkyBlockCalendar.getDay())));
                location.line(Text.key("scoreboard.skyblock.calendar_time_line",
                    SkyBlockCalendar.getDisplay(SkyBlockCalendar.getElapsed())));
                try {
                    RegionType type = region.getType();
                    String name = type.getName();
                    if (type == RegionType.PLAYER_MUSEUM) {
                        name = name.formatted(player.getUsername());
                    }
                    location.line(Text.key("scoreboard.skyblock.region_line",
                        Text.of("<color:{}>{}", type.getColor(), name)));
                } catch (NullPointerException ignored) {
                    location.line(" {}", Text.key("scoreboard.skyblock.region_unknown"));
                }

                // TODO: make classes / a manager for regions to display scoreboard information.
                boolean electionRoom = region != null && region.getType() == RegionType.ELECTION_ROOM;
                TextBody.Section election = body.section("election").when(() -> electionRoom);
                if (electionRoom) {
                    election.line(BLANK);
                    election.line(Text.key("scoreboard.skyblock.election_votes_title",
                        SkyBlockCalendar.getYear()));
                    Map<String, Long> totalVotes = ElectionManager.getElectionData().tallyVotes();
                    long maxVotes = totalVotes.values().stream().mapToLong(Long::longValue).max().orElse(1);
                    ElectionManager.getElectionData().getCandidates().forEach(candidate -> {
                        long votes = totalVotes.getOrDefault(candidate.getMayorName(), 0L);
                        int barLength = maxVotes > 0 ? (int) Math.round((votes * 15.0) / maxVotes) : 0;
                        Text bars = Text.of("<color:{0}>{1}", candidate.getColor(), "|".repeat(barLength))
                            .append("<f>{}", "|".repeat(15 - barLength));
                        election.line(Text.key("scoreboard.skyblock.election_candidate_line",
                            bars,
                            candidate.getColoredName()));
                    });
                }

                TextBody.Section purse = body.section("purse").when(() -> !electionRoom);
                if (!electionRoom) {
                    purse.line(BLANK);
                    purse.line(Text.key("scoreboard.skyblock.purse_line",
                        StringUtility.commaify(dataHandler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue())));

                    Integer bits = dataHandler.get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class).getValue();
                    boolean hasBits = bits != null && bits > 0;
                    TextBody.Section bitsLine = purse.child("bits").when(() -> hasBits);
                    if (hasBits) {
                        bitsLine.line(Text.key("scoreboard.skyblock.bits_line",
                            StringUtility.commaify(bits)));
                    }
                }

                boolean darkAuction = !electionRoom
                    && DarkAuctionHandler.isPlayerInAuction(player.getUuid())
                    && DarkAuctionHandler.getLocalState() != null
                    && DarkAuctionHandler.getLocalState().getPhase() == DarkAuctionPhase.BIDDING;
                TextBody.Section auction = body.section("dark_auction").when(() -> darkAuction);
                if (darkAuction) {
                    auction.line("<8> ");
                    DarkAuctionHandler.DarkAuctionLocalState auctionState = DarkAuctionHandler.getLocalState();
                    int timeRemaining = DarkAuctionHandler.getTimeLeft().get();

                    auction.line(Text.key("scoreboard.skyblock.dark_auction.time_left_line",
                        timeRemaining));
                    auction.line(Text.key("scoreboard.skyblock.dark_auction.current_item_label"));

                    String currentItem = auctionState.getCurrentItemType();
                    if (currentItem != null) {
                        try {
                            ItemType itemType = ItemType.valueOf(currentItem);
                            SkyBlockItem item = new SkyBlockItem(itemType);
                            auction.line(" {}",
                                Text.key("scoreboard.skyblock.dark_auction.current_item_line",
                                    item.getDisplayName()));
                        } catch (Exception e) {
                            auction.line(" {}",
                                Text.key("scoreboard.skyblock.dark_auction.current_item_line",
                                    currentItem.replace("_", " ")));
                        }
                    } else {
                        auction.line(" {}", Text.key("scoreboard.skyblock.dark_auction.waiting"));
                    }
                }

                boolean objective = !electionRoom && !darkAuction && region != null
                    && !missionData.getActiveMissions(region.getType()).isEmpty();
                TextBody.Section objectives = body.section("objective").when(() -> objective);
                if (objective) {
                    objectives.line(BLANK);
                    MissionData.ActiveMission mission = missionData.getActiveMissions(region.getType()).getFirst();
                    SkyBlockMission skyBlockMission = MissionData.getMissionClass(mission.getMissionID());

                    if (skyBlockMission instanceof LocationAssociatedMission locationAssociatedMission) {
                        objectives.line(Text.key("scoreboard.skyblock.objective_with_arrow", Text.parse(BlockUtility.getArrow(
                            player.getPosition(),
                            locationAssociatedMission.getLocation()
                        ))));
                    } else {
                        objectives.line(Text.key("scoreboard.skyblock.objective_label"));
                    }
                    objectives.line("<e>{}", mission.toString());

                    SkyBlockProgressMission progressMission = missionData.getAsProgressMission(mission.getMissionID());
                    if (progressMission != null) {
                        objectives.child("progress").line(Text.key("scoreboard.skyblock.objective_progress",
                            mission.getMissionProgress(),
                            progressMission.getMaxProgress()));
                    }
                }

                body.section("footer").line(BLANK).line(Text.key("scoreboard.common.footer"));

                scoreboard.update(player, Text.of("  {}", getSidebarName(skyblockName, false, l)), body);
            }
            return TaskSchedule.tick(4);
        });
    }

    public static void removeCache(Player player) {
        scoreboard.removeScoreboard(player);
    }

    private static Text getSidebarName(int counter, boolean isGuest, Locale locale) {
        String plain = Text.key("scoreboard.skyblock.title_base").plain(locale);
        String endText = isGuest ? " GUEST" : "";

        if (counter > 0 && counter <= 8) {
            return Text.of("<f><l>{}<6>{}<e>{}<a>{}",
                    plain.substring(0, Math.min(counter - 1, plain.length())),
                    counter <= plain.length() ? String.valueOf(plain.charAt(counter - 1)) : "",
                    counter < plain.length() ? plain.substring(counter) : "",
                    endText);
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return Text.of("<f><l>{}<a>{}", plain, endText);
        } else {
            return Text.of("<e><l>{}<a>{}", plain, endText);
        }
    }
}
