package net.swofty.type.skyblockgeneric.garden.progression;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenCore;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenPersonal;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

public final class GardenProgressionSupport {
    private GardenProgressionSupport() {
    }

    public static void apply(SkyBlockPlayer player, GardenProgressionReward reward) {
        if (player == null || reward == null) {
            return;
        }
        GardenData.GardenPersonalData personal = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_PERSONAL, DatapointGardenPersonal.class)
            .getValue();
        GardenData.GardenCoreData core = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_CORE, DatapointGardenCore.class)
            .getValue();

        switch (reward.type()) {
            case "SPOKEN_TO_NPC" -> personal.getSpokenNpcFlags().add(normalizeSpokenKey(reward.key()));
            case "PROFILE_FLAG" -> personal.getVisitorRequirementFlags().add(normalizeKey(reward.key()));
            case "ITEM_DONATED" -> personal.getDonatedItems().add(normalizeKey(reward.key()));
            case "ITEM_EXPORTED" ->
                personal.getExportedItems().merge(normalizeKey(reward.key()), Math.max(0L, reward.amount()), Long::sum);
            case "PROFILE_COUNTER" ->
                personal.getVisitorRequirementCounters().merge(normalizeKey(reward.key()), reward.amount(), Long::sum);
            case "CORE_FLAG" -> core.getVisitorRequirementFlags().add(normalizeKey(reward.key()));
            case "CORE_COUNTER" ->
                core.getVisitorRequirementCounters().merge(normalizeKey(reward.key()), reward.amount(), Long::sum);
            default -> {
            }
        }
    }

    public static void apply(SkyBlockPlayer player, GardenProgressionReward... rewards) {
        if (rewards == null) {
            return;
        }
        for (GardenProgressionReward reward : rewards) {
            apply(player, reward);
        }
    }

    public static String normalizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.trim()
            .replaceAll("[^A-Za-z0-9]+", "_")
            .replaceAll("^_+|_+$", "")
            .toUpperCase(Locale.ROOT);
    }

    public static String normalizeSpokenKey(String key) {
        return normalizeKey(key).toLowerCase(Locale.ROOT);
    }
}
