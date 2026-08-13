package net.swofty.type.skyblockgeneric.loottable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPityCounters;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PityService {
    public static long progress(SkyBlockPlayer player, PityDefinition definition) {
        return counters(player).getOrDefault(definition.id().asString(), 0L);
    }

    public static boolean guaranteesNext(SkyBlockPlayer player, PityDefinition definition) {
        return progress(player, definition) + 1 >= definition.threshold();
    }

    public static long recordAttempt(SkyBlockPlayer player, PityDefinition definition, boolean obtained) {
        Map<String, Long> counters = counters(player);
        long progress = obtained ? 0 : Math.min(definition.threshold() - 1,
                counters.getOrDefault(definition.id().asString(), 0L) + 1);
        counters.put(definition.id().asString(), progress);
        datapoint(player).setValue(counters);
        return progress;
    }

    public static void reset(SkyBlockPlayer player, PityDefinition definition) {
        Map<String, Long> counters = counters(player);
        counters.remove(definition.id().asString());
        datapoint(player).setValue(counters);
    }

    private static Map<String, Long> counters(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    private static DatapointPityCounters datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.PITY_COUNTERS, DatapointPityCounters.class);
    }
}
