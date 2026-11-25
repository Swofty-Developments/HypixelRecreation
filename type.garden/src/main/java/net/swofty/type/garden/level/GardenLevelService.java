package net.swofty.type.garden.level;

import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.skyblockgeneric.garden.GardenData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GardenLevelService {
    private Map<String, Object> config = Map.of();
    private List<Map<String, Object>> levels = List.of();

    public void reload() {
        config = GardenConfigRegistry.getConfig("levels.yml");
        levels = GardenConfigRegistry.getMapList(config, "levels");
    }

    public int getMaxLevel() {
        return GardenConfigRegistry.getInt(config, "max_level", 15);
    }

    public int getCropGrowthPerLevelPercent() {
        return GardenConfigRegistry.getInt(config, "crop_growth_per_level_percent", 10);
    }

    public int getSkyBlockXpPerLevel() {
        return GardenConfigRegistry.getInt(config, "skyblock_xp_per_level", 10);
    }

    public int getUnlockedVisitorsForLevel(int level) {
        return findLevel(level)
            .map(entry -> GardenConfigRegistry.getInt(entry, "visitor_unlocks", 0))
            .orElse(0);
    }

    public List<String> getRewardsForLevel(int level) {
        return findLevel(level)
            .map(entry -> {
                List<String> legacySummary = GardenConfigRegistry.getList(entry, "reward_summary").stream()
                    .map(String::valueOf)
                    .toList();
                if (!legacySummary.isEmpty()) {
                    return legacySummary;
                }

                List<String> rendered = new ArrayList<>();
                for (Object rawReward : GardenConfigRegistry.getList(entry, "rewards")) {
                    rendered.add(renderReward(rawReward));
                }
                return rendered;
            })
            .orElse(List.of());
    }

    public List<Map<String, Object>> getLevels() {
        return new ArrayList<>(levels);
    }

    public Map<String, Object> getLevel(int level) {
        return findLevel(level).orElse(Map.of());
    }

    private java.util.Optional<Map<String, Object>> findLevel(int level) {
        return levels.stream()
            .filter(entry -> GardenConfigRegistry.getInt(entry, "level", 0) == level)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private String renderReward(Object rawReward) {
        if (rawReward instanceof Map<?, ?> rewardMapRaw) {
            Map<String, Object> rewardMap = (Map<String, Object>) rewardMapRaw;
            String type = GardenConfigRegistry.getString(rewardMap, "type", "").toUpperCase();
            return switch (type) {
                case "VISITOR_UNLOCKS" -> "+" + GardenConfigRegistry.getInt(rewardMap, "amount", 0) + " Visitor" +
                    (GardenConfigRegistry.getInt(rewardMap, "amount", 0) == 1 ? "" : "s");
                case "CROP_UNLOCK" -> normalizeDisplay(GardenConfigRegistry.getString(rewardMap, "key", "")) + " Crop";
                case "PEST_UNLOCK" -> normalizeDisplay(GardenConfigRegistry.getString(rewardMap, "key", "")) + " Pest";
                case "CROP_UPGRADE_TIER" ->
                    "Tier " + toRoman(GardenConfigRegistry.getInt(rewardMap, "amount", 0)) + " Crop Upgrades";
                case "BARN_SKIN_UNLOCK" ->
                    normalizeDisplay(GardenConfigRegistry.getString(rewardMap, "key", "")) + " Barn Skin";
                case "GREENHOUSE_UNLOCK" -> "Greenhouse Unlock";
                case "SKYBLOCK_XP" -> "+" + GardenConfigRegistry.getInt(rewardMap, "amount", 0) + " SkyBlock XP";
                case "CROP_GROWTH" -> "+" + GardenConfigRegistry.getInt(rewardMap, "amount", 0) + " Crop Growth";
                default -> {
                    GardenData.GardenRewardState rewardState = new GardenData.GardenRewardState();
                    rewardState.setType(type);
                    rewardState.setKey(GardenConfigRegistry.getString(rewardMap, "key", ""));
                    rewardState.setAmount(GardenConfigRegistry.getLong(rewardMap, "amount", 0L));
                    rewardState.setDisplayOverride(GardenConfigRegistry.getString(rewardMap, "display", ""));
                    yield GardenServices.visitors().describeReward(rewardState);
                }
            };
        }
        return String.valueOf(rawReward);
    }

    private String normalizeDisplay(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String[] parts = key.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private String toRoman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> String.valueOf(value);
        };
    }
}
