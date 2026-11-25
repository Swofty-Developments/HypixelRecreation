package net.swofty.type.garden.chips;

import net.swofty.type.garden.config.GardenConfigRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GardenChipService {
    private Map<String, Object> config = Map.of();
    private List<Map<String, Object>> chips = List.of();
    private final Map<Integer, Long> levelCosts = new HashMap<>();
    private final Map<String, Double> greenhouseMultipliers = new HashMap<>();

    public void reload() {
        config = GardenConfigRegistry.getConfig("chips.yml");
        chips = GardenConfigRegistry.getMapList(config, "chips");

        levelCosts.clear();
        for (Map<String, Object> level : GardenConfigRegistry.getMapList(config, "level_costs")) {
            levelCosts.put(
                GardenConfigRegistry.getInt(level, "level", 1),
                GardenConfigRegistry.getLong(level, "cost", 0)
            );
        }

        greenhouseMultipliers.clear();
        GardenConfigRegistry.getSection(config, "greenhouse_crop_multipliers")
            .forEach((key, value) -> greenhouseMultipliers.put(key, Double.parseDouble(String.valueOf(value))));
    }

    public String resolveRarity(int consumed) {
        if (consumed >= 16) {
            return "LEGENDARY";
        }
        if (consumed >= 4) {
            return "EPIC";
        }
        if (consumed >= 1) {
            return "RARE";
        }
        return "LOCKED";
    }

    public double calculateGardenSowdust(double totalFortune, boolean doubleBreakCrop) {
        double multiplier = doubleBreakCrop ? 2D : 1D;
        return Math.ceil(totalFortune / 100D) / multiplier;
    }

    public double calculateGreenhouseSowdust(Map<String, Integer> baseCropAmounts, double totalFortune) {
        double total = 0D;
        double fortuneMultiplier = 1D + (totalFortune / 100D);
        for (Map.Entry<String, Integer> entry : baseCropAmounts.entrySet()) {
            double cropMultiplier = greenhouseMultipliers.getOrDefault(entry.getKey(), 1D);
            long normalized = Math.round(entry.getValue() / (5D * cropMultiplier));
            total += 5D * normalized * fortuneMultiplier;
        }
        return total;
    }

    public long getLevelCost(int level) {
        return levelCosts.getOrDefault(level, 0L);
    }

    public List<Map<String, Object>> getChips() {
        return chips;
    }

    public Map<String, Object> getChip(String id) {
        return chips.stream()
            .filter(entry -> GardenConfigRegistry.getString(entry, "id", "").equalsIgnoreCase(id))
            .findFirst()
            .orElse(Map.of());
    }

    public int getMaxLevel(String rarity) {
        return switch (rarity == null ? "LOCKED" : rarity.toUpperCase()) {
            case "RARE" -> 10;
            case "EPIC" -> 15;
            case "LEGENDARY" -> 20;
            default -> 0;
        };
    }
}
