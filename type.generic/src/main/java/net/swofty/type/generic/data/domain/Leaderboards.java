package net.swofty.type.generic.data.domain;

import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.leaderboard.LeaderboardTracked;
import net.swofty.type.generic.leaderboard.MapLeaderboardTracked;

import java.util.Map;
import java.util.UUID;

public final class Leaderboards {

    private Leaderboards() {}

    public static void sync(UUID uuid, DataHandler handler) {
        if (!LeaderboardService.isInitialized()) return;

        for (Datapoint<?> datapoint : handler.getDatapoints().values()) {
            if (datapoint instanceof LeaderboardTracked tracked) {
                String key = tracked.getLeaderboardKey();
                if (key != null) {
                    LeaderboardService.updateScore(key, uuid, tracked.getLeaderboardScore());
                }
            }

            if (datapoint instanceof MapLeaderboardTracked mapTracked) {
                Map<String, Double> scores = mapTracked.getAllLeaderboardScores();
                for (Map.Entry<String, Double> entry : scores.entrySet()) {
                    LeaderboardService.updateScore(mapTracked.getLeaderboardKeyFor(entry.getKey()), uuid, entry.getValue());
                }
            }
        }
    }
}
