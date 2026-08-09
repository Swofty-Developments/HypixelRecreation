package net.swofty.type.skyblockgeneric.elections;

import com.google.gson.Gson;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.election.CastVoteProtocol;
import net.swofty.commons.protocol.objects.election.GetCandidatesProtocol;
import net.swofty.commons.protocol.objects.election.GetElectionDataProtocol;
import net.swofty.commons.protocol.objects.election.GetPlayerVoteProtocol;
import net.swofty.commons.protocol.objects.election.ResolveElectionProtocol;
import net.swofty.commons.protocol.objects.election.StartElectionProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ElectionManager {

    private static final Gson GSON = new Gson();
    private static ProxyService SERVICE;
    private static final ConcurrentHashMap<UUID, String> playerVoteCache = new ConcurrentHashMap<>();
    private static final AtomicBoolean tallyRefreshStarted = new AtomicBoolean();

    @Getter
    private static ElectionData electionData = new ElectionData();

    public static void loadFromService() {
        SERVICE = new ProxyService(ServiceType.ELECTION);
        if (!SERVICE.isOnline().join()) {
            Logger.error("Service ELECTION is not online!");
            electionData = new ElectionData();
            initializeFirstElection();
            startTallyRefreshTask();
            return;
        }

        try {
            GetElectionDataProtocol.GetElectionDataResponse response =
                SERVICE.<GetElectionDataProtocol.GetElectionDataMessage,
                    GetElectionDataProtocol.GetElectionDataResponse>handleRequest(
                    new GetElectionDataProtocol.GetElectionDataMessage()
                ).join();

            if (response.found() && response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
                if (electionData.getCurrentMayor() == null) {
                    electionData = new ElectionData();
                    initializeFirstElection();
                    Logger.info("Election data was missing mayor info. Reinitialized first election.");
                }
            } else {
                electionData = new ElectionData();
                initializeFirstElection();
                Logger.info("No election data found. Initialized first election.");
            }
        } catch (Exception e) {
            Logger.error("Failed to load election data from service: {}", e.getMessage());
            electionData = new ElectionData();
            initializeFirstElection();
        }

        startTallyRefreshTask();
    }

    private static void initializeFirstElection() {
        int currentYear = SkyBlockCalendar.getYear();
        electionData.setCurrentMayor(SkyBlockMayor.COLE.name());
        electionData.setCurrentMayorColor(ElectionData.colorForIndex(4));
        electionData.setCurrentMayorPerks(Arrays.stream(SkyBlockMayor.COLE.getAllPerks()).map(Enum::name).toList());
        electionData.setMayorElectedYear(currentYear);
        electionData.setCurrentMinister(SkyBlockMayor.DIAZ.name());
        electionData.setMinisterPerk(SkyBlockMayor.Perk.STOCK_EXCHANGE.name());
        electionData.setCurrentMinisterColor(ElectionData.colorForIndex(0));
    }

    public static void onElectionStart() {
        int currentYear = SkyBlockCalendar.getYear();
        if (electionData.isElectionOpen()) return;

        electionData.startNewElection(currentYear);
        String serialized = GSON.toJson(electionData);

        try {
            StartElectionProtocol.StartElectionResponse response =
                SERVICE.<StartElectionProtocol.StartElectionMessage,
                    StartElectionProtocol.StartElectionResponse>handleRequest(
                    new StartElectionProtocol.StartElectionMessage(currentYear, serialized)
                ).join();

            if (response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
            }

            if (response.started()) {
                Logger.info("Election started for Year {}", currentYear);
            } else {
                Logger.info("Election for Year {} was already started by another server", currentYear);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to start election via service");
        }
    }

    public static void onElectionEnd() {
        if (!electionData.isElectionOpen()) return;

        int currentYear = SkyBlockCalendar.getYear();

        try {
            ResolveElectionProtocol.ResolveElectionResponse response =
                SERVICE.<ResolveElectionProtocol.ResolveElectionMessage,
                    ResolveElectionProtocol.ResolveElectionResponse>handleRequest(
                    new ResolveElectionProtocol.ResolveElectionMessage(currentYear)
                ).join();

            if (response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
            }

            if (response.resolved()) {
                Logger.info("Election resolved for Year {}. New Mayor: {}", currentYear, electionData.getCurrentMayor());
            } else {
                Logger.info("Election for Year {} was already resolved by another server", currentYear);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to resolve election via service");
        }

        playerVoteCache.clear();
    }

    public static CompletableFuture<Void> castVote(UUID accountId, String candidateName) {
        if (!electionData.isElectionOpen()) return CompletableFuture.completedFuture(null);

        boolean validCandidate = electionData.getCandidates().stream()
            .anyMatch(c -> c.getMayorName().equals(candidateName));
        if (!validCandidate) return CompletableFuture.completedFuture(null);

        return SERVICE.<CastVoteProtocol.CastVoteMessage,
            CastVoteProtocol.CastVoteResponse>handleRequest(
            new CastVoteProtocol.CastVoteMessage(accountId, candidateName)
        ).thenAccept(response -> {
            if (response.success()) {
                playerVoteCache.put(accountId, candidateName);
                if (response.tallies() != null) {
                    electionData.updateTallies(response.tallies());
                }
            }
        }).exceptionally(e -> {
            Logger.error(e, "Failed to cast vote via service");
            return null;
        });
    }

    public static String getPlayerVote(UUID accountId) {
        return playerVoteCache.get(accountId);
    }

    public static CompletableFuture<String> fetchPlayerVote(UUID accountId) {
        String cached = playerVoteCache.get(accountId);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return SERVICE.<GetPlayerVoteProtocol.GetPlayerVoteMessage,
            GetPlayerVoteProtocol.GetPlayerVoteResponse>handleRequest(
            new GetPlayerVoteProtocol.GetPlayerVoteMessage(accountId)
        ).thenApply(response -> {
            if (response.candidateName() != null) {
                playerVoteCache.put(accountId, response.candidateName());
            }
            return response.candidateName();
        });
    }

    public static void clearPlayerVoteCache(UUID accountId) {
        playerVoteCache.remove(accountId);
    }

    public static SkyBlockMayor getCurrentMayor() {
        return electionData.getMayorEnum();
    }

    public static SkyBlockMayor getCurrentMinister() {
        return electionData.getMinisterEnum();
    }

    public static boolean isMayorPerkActive(SkyBlockMayor.Perk perk) {
        return electionData.getCurrentMayorPerkEnums().contains(perk);
    }

    public static boolean isMinisterPerkActive(SkyBlockMayor.Perk perk) {
        SkyBlockMayor.Perk activePerk = electionData.getMinisterPerkEnum();
        return activePerk != null && activePerk == perk;
    }

    public static boolean isPerkActive(SkyBlockMayor.Perk perk) {
        return isMayorPerkActive(perk) || isMinisterPerkActive(perk);
    }

    public static CompletableFuture<List<GetCandidatesProtocol.CandidateInfo>> fetchCandidates() {
        return SERVICE.<GetCandidatesProtocol.GetCandidatesMessage,
            GetCandidatesProtocol.GetCandidatesResponse>handleRequest(
            new GetCandidatesProtocol.GetCandidatesMessage()
        ).thenApply(GetCandidatesProtocol.GetCandidatesResponse::candidates);
    }

    public static void checkElectionCycle() {
        int currentYear = SkyBlockCalendar.getYear();
        int currentMonth = SkyBlockCalendar.getMonth();

        if (currentMonth >= 6 && currentMonth <= 8 && !electionData.isElectionOpen()) {
            if (electionData.getElectionYear() != currentYear) {
                onElectionStart();
            }
        }

        if (currentMonth >= 9 && electionData.isElectionOpen()) {
            onElectionEnd();
        }
    }

    private static void startTallyRefreshTask() {
        if (!tallyRefreshStarted.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (electionData.isElectionOpen()) {
                refreshTalliesAsync();
            }
            return TaskSchedule.seconds(30);
        });
    }

    private static void refreshTalliesAsync() {
        SERVICE.<GetCandidatesProtocol.GetCandidatesMessage,
            GetCandidatesProtocol.GetCandidatesResponse>handleRequest(
            new GetCandidatesProtocol.GetCandidatesMessage()
        ).thenAccept(response -> {
            if (response.electionOpen()) {
                Map<String, Long> tallies = new HashMap<>();
                for (GetCandidatesProtocol.CandidateInfo info : response.candidates()) {
                    tallies.put(info.mayorName(), info.votes());
                }
                electionData.updateTallies(tallies);
            }
        }).exceptionally(e -> {
            Logger.warn("Failed to refresh election tallies: {}", e.getMessage());
            return null;
        });
    }
}
