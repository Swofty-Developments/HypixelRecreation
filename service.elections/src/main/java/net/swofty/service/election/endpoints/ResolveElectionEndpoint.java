package net.swofty.service.election.endpoints;

import com.google.gson.Gson;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.text.Text;
import net.swofty.commons.protocol.objects.election.ResolveElectionProtocol;
import net.swofty.service.election.ElectionDatabase;
import net.swofty.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.swofty.commons.redis.RedisMessageContext;

public class ResolveElectionEndpoint implements RedisMessageHandler
        <ResolveElectionProtocol.ResolveElectionMessage,
                ResolveElectionProtocol.ResolveElectionResponse> {

    private static final Gson GSON = new Gson();

    private static final List<String> SPECIAL_MAYORS = List.of("SCORPIUS", "DERPY", "JERRY");

    private static final List<NamedTextColor> CANDIDATE_COLORS = List.of(
            NamedTextColor.RED, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.YELLOW, NamedTextColor.LIGHT_PURPLE);

    @Override
    public RedisProtocol<ResolveElectionProtocol.ResolveElectionMessage,
            ResolveElectionProtocol.ResolveElectionResponse> protocol() {
        return new ResolveElectionProtocol();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResolveElectionProtocol.ResolveElectionResponse handle(ResolveElectionProtocol.ResolveElectionMessage messageObject, RedisMessageContext context) {
        try {
            String rawData = ElectionDatabase.loadElectionData();
            if (rawData == null) {
                return new ResolveElectionProtocol.ResolveElectionResponse(false, null, true, null);
            }

            Map<String, Object> data = GSON.fromJson(rawData, Map.class);
            Boolean electionOpen = (Boolean) data.get("electionOpen");

            if (electionOpen == null || !electionOpen) {
                data.remove("votes");
                return new ResolveElectionProtocol.ResolveElectionResponse(false, GSON.toJson(data), true, null);
            }

            int electionYear = messageObject.year();
            Map<String, Long> tallies = ElectionDatabase.getTallies(electionYear);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) data.get("candidates");
            if (candidates == null) candidates = new ArrayList<>();

            for (Map<String, Object> c : candidates) {
                String name = (String) c.get("mayorName");
                tallies.putIfAbsent(name, 0L);
            }

            List<Map.Entry<String, Long>> sorted = new ArrayList<>(tallies.entrySet());
            sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

            if (sorted.isEmpty()) {
                return new ResolveElectionProtocol.ResolveElectionResponse(false, GSON.toJson(data), true, null);
            }

            long topVotes = sorted.getFirst().getValue();
            List<Map.Entry<String, Long>> tied = sorted.stream()
                    .filter(e -> e.getValue() == topVotes).toList();

            String winnerName;
            if (tied.size() > 1) {
                winnerName = tied.get(ThreadLocalRandom.current().nextInt(tied.size())).getKey();
            } else {
                winnerName = sorted.getFirst().getKey();
            }

            Map<String, Object> winnerCandidate = candidates.stream()
                    .filter(c -> winnerName.equals(c.get("mayorName")))
                    .findFirst().orElse(null);

            int winnerIndex = winnerCandidate != null
                    ? ((Number) winnerCandidate.getOrDefault("index", 0)).intValue()
                    : 0;

            boolean isSpecialWinner = SPECIAL_MAYORS.contains(winnerName);

            data.put("currentMayor", winnerName);
            data.put("currentMayorColor", wireColorForIndex(winnerIndex));

            if (winnerCandidate != null && winnerCandidate.get("activePerks") != null) {
                data.put("currentMayorPerks", winnerCandidate.get("activePerks"));
            } else {
                data.put("currentMayorPerks", List.of());
            }
            data.put("mayorElectedYear", electionYear);

            Map<String, Object> lastElectedYear = (Map<String, Object>) data.getOrDefault("lastElectedYear", new HashMap<>());
            lastElectedYear.put(winnerName, electionYear);
            data.put("lastElectedYear", lastElectedYear);

            if (isSpecialWinner) {
                data.put("currentMinister", null);
                data.put("ministerPerk", null);
                data.put("currentMinisterColor", null);
            } else {
                String secondPlace = sorted.stream()
                        .filter(e -> !e.getKey().equals(winnerName))
                        .findFirst().map(Map.Entry::getKey).orElse(null);

                if (secondPlace != null) {
                    Map<String, Object> ministerCandidate = candidates.stream()
                            .filter(c -> secondPlace.equals(c.get("mayorName")))
                            .findFirst().orElse(null);

                    int ministerIndex = ministerCandidate != null
                            ? ((Number) ministerCandidate.getOrDefault("index", 1)).intValue()
                            : 1;

                    data.put("currentMinister", secondPlace);
                    data.put("currentMinisterColor", wireColorForIndex(ministerIndex));

                    List<String> ministerPerks = ministerCandidate != null
                            ? (List<String>) ministerCandidate.get("activePerks")
                            : null;
                    if (ministerPerks != null && !ministerPerks.isEmpty()) {
                        data.put("ministerPerk", ministerPerks.get(
                                ThreadLocalRandom.current().nextInt(ministerPerks.size())));
                    }
                }
            }

            updatePerkProgression(data, candidates, winnerName);

            long totalVoteCount = tallies.values().stream().mapToLong(Long::longValue).sum();
            Map<String, Object> electionResult = new HashMap<>();
            electionResult.put("year", electionYear);
            List<Map<String, Object>> candidateResults = new ArrayList<>();
            for (Map.Entry<String, Long> entry : sorted) {
                Map<String, Object> cr = new HashMap<>();
                cr.put("mayorName", entry.getKey());
                cr.put("votes", entry.getValue());
                cr.put("percentage", totalVoteCount > 0 ? (entry.getValue() * 100.0) / totalVoteCount : 0);
                candidateResults.add(cr);
            }
            electionResult.put("candidateResults", candidateResults);
            data.put("lastElectionResult", electionResult);

            data.put("candidates", List.of());
            data.remove("votes");
            data.put("voteTallies", Map.of());
            data.put("electionOpen", false);

            ElectionDatabase.saveElectionData(GSON.toJson(data));

            return new ResolveElectionProtocol.ResolveElectionResponse(true, GSON.toJson(data), true, null);
        } catch (Exception e) {
            Logger.error(e, "Failed to resolve election");
            return new ResolveElectionProtocol.ResolveElectionResponse(false, null, true, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void updatePerkProgression(Map<String, Object> data,
                                       List<Map<String, Object>> candidates,
                                       String winnerName) {
        Map<String, Object> candidateActivePerks = (Map<String, Object>) data.getOrDefault("candidateActivePerks", new HashMap<>());
        Map<String, Object> failedPerkGainLastTime = (Map<String, Object>) data.getOrDefault("failedPerkGainLastTime", new HashMap<>());

        for (Map<String, Object> candidate : candidates) {
            String name = (String) candidate.get("mayorName");
            if (name.equals(winnerName)) {
                candidateActivePerks.remove(winnerName);
                failedPerkGainLastTime.remove(winnerName);
                continue;
            }

            if (SPECIAL_MAYORS.contains(name)) continue;

            List<String> currentPerks = candidate.get("activePerks") != null
                    ? new ArrayList<>((List<String>) candidate.get("activePerks"))
                    : new ArrayList<>();

            int maxPerks = estimateMaxPerks(name);
            if (currentPerks.size() < maxPerks) {
                Boolean failedLast = (Boolean) failedPerkGainLastTime.get(name);
                boolean gainPerk = Boolean.TRUE.equals(failedLast) || ThreadLocalRandom.current().nextBoolean();

                if (gainPerk) {
                    failedPerkGainLastTime.put(name, false);
                } else {
                    failedPerkGainLastTime.put(name, true);
                }
            }
            candidateActivePerks.put(name, currentPerks);
        }

        if (!SPECIAL_MAYORS.contains(winnerName)) {
            List<String> winnerCurrentPerks = (List<String>) candidateActivePerks.getOrDefault(winnerName, new ArrayList<>());
            if (winnerCurrentPerks.isEmpty()) {
                List<String> winnerPerksFromCandidate = candidates.stream()
                        .filter(c -> winnerName.equals(c.get("mayorName")))
                        .findFirst()
                        .map(c -> (List<String>) c.get("activePerks"))
                        .orElse(List.of());
                if (!winnerPerksFromCandidate.isEmpty()) {
                    String randomPerk = winnerPerksFromCandidate.get(
                            ThreadLocalRandom.current().nextInt(winnerPerksFromCandidate.size()));
                    candidateActivePerks.put(winnerName, List.of(randomPerk));
                }
            }
        }

        data.put("candidateActivePerks", candidateActivePerks);
        data.put("failedPerkGainLastTime", failedPerkGainLastTime);
    }

    private int estimateMaxPerks(String mayorName) {
        return switch (mayorName) {
            case "PAUL" -> 3;
            case "SCORPIUS" -> 2;
            case "JERRY" -> 3;
            default -> 4;
        };
    }

    private static TextColor colorForIndex(int index) {
        return index >= 0 && index < CANDIDATE_COLORS.size() ? CANDIDATE_COLORS.get(index) : NamedTextColor.WHITE;
    }

    private static String wireColorForIndex(int index) {
        return Text.colorTag(colorForIndex(index));
    }
}
