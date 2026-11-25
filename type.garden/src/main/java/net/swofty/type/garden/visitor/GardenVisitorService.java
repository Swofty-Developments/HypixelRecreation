package net.swofty.type.garden.visitor;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.skyblockgeneric.garden.GardenData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GardenVisitorService {
    private Map<String, Object> config = Map.of();
    private final Map<String, Object> rarityWeights = new HashMap<>();
    private final Map<Integer, int[]> baseItemsByLevel = new HashMap<>();
    private final Map<String, Double> quantityMultipliers = new HashMap<>();
    private final Map<String, Double> requestRarityMultipliers = new HashMap<>();
    private final Map<String, Double> farmingXpRarityMultipliers = new HashMap<>();
    private final Map<String, Double> itemFarmingXpAmounts = new HashMap<>();
    private final Map<String, Double> copperRarityMultipliers = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> gardenXpByLevel = new HashMap<>();
    private final Map<String, String> requestItemAliases = new HashMap<>();
    private final Map<String, List<CompactionProfile>> requestCompactionProfiles = new HashMap<>();
    private List<Map<String, Object>> bonusRewards = List.of();
    private List<Map<String, Object>> visitors = List.of();

    public void reload() {
        config = GardenConfigRegistry.getConfig("visitors.yml");
        visitors = GardenConfigRegistry.getMapList(config, "registry");
        bonusRewards = GardenConfigRegistry.getMapList(config, "bonus_rewards");

        rarityWeights.clear();
        rarityWeights.putAll(GardenConfigRegistry.getSection(config, "rarity_weights"));

        quantityMultipliers.clear();
        GardenConfigRegistry.getSection(config, "item_quantity_multipliers")
            .forEach((key, value) -> quantityMultipliers.put(normalizeItemKey(key), Double.parseDouble(String.valueOf(value))));

        requestItemAliases.clear();
        GardenConfigRegistry.getSection(config, "request_item_aliases")
            .forEach((key, value) -> requestItemAliases.put(normalizeItemKey(key), normalizeItemKey(String.valueOf(value))));

        requestRarityMultipliers.clear();
        GardenConfigRegistry.getSection(config, "rarity_request_multipliers")
            .forEach((key, value) -> requestRarityMultipliers.put(normalizeItemKey(key), Double.parseDouble(String.valueOf(value))));

        farmingXpRarityMultipliers.clear();
        GardenConfigRegistry.getSection(config, "farming_xp_rarity_multipliers")
            .forEach((key, value) -> farmingXpRarityMultipliers.put(normalizeItemKey(key), Double.parseDouble(String.valueOf(value))));

        itemFarmingXpAmounts.clear();
        GardenConfigRegistry.getSection(config, "item_farming_xp_amounts")
            .forEach((key, value) -> itemFarmingXpAmounts.put(normalizeItemKey(key), Double.parseDouble(String.valueOf(value))));

        copperRarityMultipliers.clear();
        GardenConfigRegistry.getSection(config, "copper_rarity_multipliers")
            .forEach((key, value) -> copperRarityMultipliers.put(normalizeItemKey(key), Double.parseDouble(String.valueOf(value))));

        baseItemsByLevel.clear();
        for (Map<String, Object> entry : GardenConfigRegistry.getMapList(config, "base_item_ranges")) {
            int level = GardenConfigRegistry.getInt(entry, "level", 1);
            baseItemsByLevel.put(level, new int[]{
                GardenConfigRegistry.getInt(entry, "min", 500),
                GardenConfigRegistry.getInt(entry, "max", 1000)
            });
        }

        gardenXpByLevel.clear();
        for (Map<String, Object> entry : GardenConfigRegistry.getMapList(config, "garden_xp_by_level")) {
            int level = GardenConfigRegistry.getInt(entry, "level", 1);
            Map<String, Integer> rarityMap = new HashMap<>();
            rarityMap.put("UNCOMMON", GardenConfigRegistry.getInt(entry, "uncommon", 2));
            rarityMap.put("RARE", GardenConfigRegistry.getInt(entry, "rare", rarityMap.get("UNCOMMON")));
            rarityMap.put("LEGENDARY", GardenConfigRegistry.getInt(entry, "legendary", rarityMap.get("UNCOMMON")));
            rarityMap.put("MYTHIC", GardenConfigRegistry.getInt(entry, "mythic", rarityMap.get("LEGENDARY")));
            rarityMap.put("SPECIAL", GardenConfigRegistry.getInt(entry, "special", rarityMap.get("LEGENDARY")));
            gardenXpByLevel.put(level, rarityMap);
        }

        requestCompactionProfiles.clear();
        for (Map.Entry<String, Object> entry : GardenConfigRegistry.getSection(config, "request_compaction").entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> profileSectionRaw)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> profileSection = (Map<String, Object>) profileSectionRaw;
            List<CompactionProfile> profiles = new ArrayList<>();
            for (Map<String, Object> profileConfig : GardenConfigRegistry.getMapList(profileSection, "profiles")) {
                List<CompactionStage> stages = GardenConfigRegistry.getMapList(profileConfig, "stages").stream()
                    .map(stageConfig -> new CompactionStage(
                        normalizeItemKey(GardenConfigRegistry.getString(stageConfig, "item", "")),
                        Math.max(1, GardenConfigRegistry.getInt(stageConfig, "raw_cost", 1))
                    ))
                    .filter(stage -> !stage.itemId().isBlank())
                    .sorted(Comparator.comparingInt(CompactionStage::rawCost))
                    .toList();
                if (stages.isEmpty()) {
                    continue;
                }
                profiles.add(new CompactionProfile(
                    normalizeItemKey(GardenConfigRegistry.getString(profileConfig, "id", entry.getKey())),
                    GardenConfigRegistry.getDouble(profileConfig, "weight", 1D),
                    stages
                ));
            }
            if (!profiles.isEmpty()) {
                requestCompactionProfiles.put(normalizeItemKey(entry.getKey()), List.copyOf(profiles));
            }
        }
    }

    public int getArrivalMinutes(int servedUniqueVisitors) {
        Map<String, Object> arrivalMinutes = GardenConfigRegistry.getSection(config, "arrival_minutes");
        if (servedUniqueVisitors >= 100) {
            return GardenConfigRegistry.getInt(arrivalMinutes, "one_hundred_unique", 9);
        }
        if (servedUniqueVisitors >= 50) {
            return GardenConfigRegistry.getInt(arrivalMinutes, "fifty_unique", 12);
        }
        return GardenConfigRegistry.getInt(arrivalMinutes, "base", 15);
    }

    public int[] getBaseItemsRange(int gardenLevel) {
        return baseItemsByLevel.getOrDefault(gardenLevel, baseItemsByLevel.getOrDefault(1, new int[]{500, 1000}));
    }

    public double getQuantityMultiplier(String itemId) {
        return quantityMultipliers.getOrDefault(normalizeItemKey(itemId), 1D);
    }

    public String resolveRequestedItemKey(String itemId) {
        String normalized = normalizeItemKey(itemId);
        return requestItemAliases.getOrDefault(normalized, normalized);
    }

    public String getRepresentativeRequestItem(String wantedItem) {
        String resolvedWantedItem = resolveRequestedItemKey(wantedItem);
        List<CompactionProfile> profiles = requestCompactionProfiles.get(resolvedWantedItem);
        if (profiles == null || profiles.isEmpty()) {
            return resolvedWantedItem;
        }
        List<CompactionStage> stages = profiles.getFirst().stages();
        return stages.isEmpty() ? resolvedWantedItem : stages.getFirst().itemId();
    }

    public CompactedRequest compactRequest(String wantedItem, int rawAmount) {
        int sanitizedRawAmount = Math.max(1, rawAmount);
        String resolvedWantedItem = resolveRequestedItemKey(wantedItem);
        List<CompactionProfile> profiles = requestCompactionProfiles.get(resolvedWantedItem);
        if (profiles == null || profiles.isEmpty()) {
            return new CompactedRequest(resolvedWantedItem, sanitizedRawAmount, sanitizedRawAmount);
        }

        CompactionProfile profile = chooseCompactionProfile(profiles);
        CompactionStage selectedStage = profile.stages().getFirst();
        for (CompactionStage stage : profile.stages()) {
            if (stage.rawCost() <= sanitizedRawAmount) {
                selectedStage = stage;
            } else {
                break;
            }
        }

        int compactedAmount = Math.max(1, sanitizedRawAmount / selectedStage.rawCost());
        int remainingRaw = sanitizedRawAmount % selectedStage.rawCost();
        if (selectedStage.rawCost() > 1
            && remainingRaw > 0
            && remainingRaw < sanitizedRawAmount * 0.10D) {
            compactedAmount++;
        }
        return new CompactedRequest(selectedStage.itemId(), compactedAmount, sanitizedRawAmount);
    }

    public double getRequestMultiplier(String rarity) {
        return requestRarityMultipliers.getOrDefault(normalizeItemKey(rarity), 1D);
    }

    public double getItemFarmingXpAmount(String itemId) {
        String normalized = normalizeItemKey(itemId);
        return itemFarmingXpAmounts.getOrDefault(normalized, getQuantityMultiplier(normalized));
    }

    public double calculateFarmingXp(int baseItems, double itemFarmingXp, String rarity) {
        return baseItems * itemFarmingXp * farmingXpRarityMultipliers.getOrDefault(normalizeItemKey(rarity), 0.05D);
    }

    public int calculateGardenXp(int gardenLevel, String rarity) {
        Map<String, Integer> rarityMap = gardenXpByLevel.getOrDefault(gardenLevel, gardenXpByLevel.getOrDefault(1, Map.of()));
        return rarityMap.getOrDefault(normalizeItemKey(rarity), rarityMap.getOrDefault("UNCOMMON", 2));
    }

    public int calculateCopper(int baseItems, double itemQuantityMultiplier, String rarity) {
        double copper = (((double) baseItems / 2000D) / itemQuantityMultiplier)
            * copperRarityMultipliers.getOrDefault(normalizeItemKey(rarity), 1D)
            * 1.3D;
        return Math.max(2, (int) Math.floor(copper));
    }

    public double getRarityWeight(String rarity) {
        Object weight = rarityWeights.get(normalizeItemKey(rarity));
        return weight == null ? 0 : Double.parseDouble(String.valueOf(weight));
    }

    public int getBitsBase() {
        return GardenConfigRegistry.getInt(config, "bits_base", 5);
    }

    public int getMaxVisibleVisitors() {
        return GardenConfigRegistry.getInt(config, "max_visible_visitors", 5);
    }

    public int getMaxQueuedVisitors() {
        return GardenConfigRegistry.getInt(config, "max_queued_visitors", 1);
    }

    public int getExpectedUniqueVisitors() {
        return GardenConfigRegistry.getInt(config, "expected_unique_visitors", 137);
    }

    public int getFarmingSpeedupMultiplier() {
        return GardenConfigRegistry.getInt(config, "farming_speedup_multiplier", 3);
    }

    public List<Map<String, Object>> getVisitors() {
        return visitors;
    }

    public List<Map<String, Object>> getBonusRewards() {
        return bonusRewards;
    }

    public List<GardenData.GardenRewardState> getConfiguredRewards(Map<String, Object> root, String key) {
        List<GardenData.GardenRewardState> rewards = new ArrayList<>();
        for (Object rawReward : GardenConfigRegistry.getList(root, key)) {
            GardenData.GardenRewardState rewardState = parseReward(rawReward);
            if (rewardState != null) {
                rewards.add(rewardState);
            }
        }
        return rewards;
    }

    public GardenData.GardenRewardState parseReward(Object rawReward) {
        if (rawReward == null) {
            return null;
        }
        if (rawReward instanceof GardenData.GardenRewardState rewardState) {
            return rewardState;
        }
        if (rawReward instanceof Map<?, ?> rewardMap) {
            return parseRewardMap(rewardMap);
        }
        return parseLegacyReward(String.valueOf(rawReward));
    }

    public List<VisitorRequirement> getRequirements(Map<String, Object> definition) {
        List<VisitorRequirement> requirements = new ArrayList<>();
        for (Object rawRequirement : GardenConfigRegistry.getList(definition, "requirements")) {
            VisitorRequirement requirement = parseRequirement(rawRequirement);
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }

    public VisitorRequirement parseRequirement(Object rawRequirement) {
        if (rawRequirement == null) {
            return null;
        }
        if (rawRequirement instanceof Map<?, ?> requirementMapRaw) {
            Map<String, Object> requirementMap = castMap(requirementMapRaw);
            String type = normalizeItemKey(GardenConfigRegistry.getString(requirementMap, "type", ""));
            String key = normalizeItemKey(GardenConfigRegistry.getString(requirementMap, "key", ""));
            long amount = GardenConfigRegistry.getLong(requirementMap, "amount", 0L);
            String secondaryKey = normalizeItemKey(GardenConfigRegistry.getString(requirementMap, "secondary_key", ""));
            String display = GardenConfigRegistry.getString(requirementMap, "display", "");
            return new VisitorRequirement(type, key, amount, secondaryKey, display);
        }

        String requirement = String.valueOf(rawRequirement).trim();
        if (requirement.isBlank()) {
            return null;
        }
        if (requirement.regionMatches(true, 0, "Talk to", 0, "Talk to".length())) {
            String normalizedNpc = normalizeItemKey(requirement
                .replaceFirst("(?i)^Talk to\\s+", "")
                .replaceFirst("(?i)^the\\s+", "")
                .replaceFirst("(?i)^this\\s+", ""));
            return new VisitorRequirement("SPOKEN_TO_NPC", normalizedNpc, 1L, "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Garden ", 0, "Garden ".length())) {
            return new VisitorRequirement("GARDEN_LEVEL_AT_LEAST", "", parseTrailingRoman(requirement), "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Farming ", 0, "Farming ".length())) {
            return new VisitorRequirement("SKILL_LEVEL_AT_LEAST", "FARMING", parseTrailingRoman(requirement), "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Fishing ", 0, "Fishing ".length())) {
            return new VisitorRequirement("SKILL_LEVEL_AT_LEAST", "FISHING", parseTrailingRoman(requirement), "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Donate ", 0, "Donate ".length())) {
            return new VisitorRequirement("ITEM_DONATED", normalizeItemKey(requirement.replaceFirst("(?i)^Donate\\s+", "")), 1L, "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Save ", 0, "Save ".length())) {
            return new VisitorRequirement("PROFILE_FLAG", normalizeItemKey(requirement), 1L, "", requirement);
        }
        if (requirement.regionMatches(true, 0, "Export ", 0, "Export ".length())) {
            String exportRequirement = requirement.replaceFirst("(?i)^Export\\s+", "");
            String[] tokens = exportRequirement.split("x\\s+", 2);
            if (tokens.length == 2) {
                long amount = parseLongValue(tokens[0].replace(",", ""), 0L);
                return new VisitorRequirement("ITEM_EXPORTED", normalizeItemKey(tokens[1]), amount, "", requirement);
            }
        }
        if (requirement.contains("Blooming Business")) {
            return new VisitorRequirement("MAYOR_PERK_ACTIVE", "BLOOMING_BUSINESS", 1L, "", requirement);
        }
        return new VisitorRequirement("PROFILE_FLAG", normalizeItemKey(requirement), 1L, "", requirement);
    }

    public String renderRequirement(VisitorRequirement requirement) {
        if (requirement == null) {
            return "Requirement data unavailable";
        }
        if (!requirement.display().isBlank()) {
            return requirement.display();
        }
        return switch (requirement.type()) {
            case "GARDEN_LEVEL_AT_LEAST" -> "Garden " + toRoman((int) Math.max(1L, requirement.amount()));
            case "SKILL_LEVEL_AT_LEAST" ->
                capitalizeWords(requirement.key()) + " " + toRoman((int) Math.max(1L, requirement.amount()));
            case "SPOKEN_TO_NPC" -> "Talk to " + capitalizeWords(requirement.key());
            case "ITEM_DONATED" -> "Donate " + capitalizeWords(requirement.key());
            case "ITEM_EXPORTED" -> "Export " + requirement.amount() + "x " + capitalizeWords(requirement.key());
            case "MAYOR_PERK_ACTIVE" -> "Blooming Business";
            default -> capitalizeWords(requirement.key());
        };
    }

    public String describeReward(GardenData.GardenRewardState reward) {
        if (reward == null) {
            return "";
        }
        String displayOverride = reward.getDisplayOverride();
        if (displayOverride != null && !displayOverride.isBlank()) {
            return displayOverride;
        }
        long amount = reward.getAmount() > 0 ? reward.getAmount() : reward.getMin();
        return switch (normalizeItemKey(reward.getType())) {
            case "ITEM" -> "+" + amount + "x " + capitalizeWords(reward.getKey());
            case "BITS" -> "+" + amount + " Bits";
            case "COPPER" -> "+" + amount + " Copper";
            case "FARMING_XP" -> "+" + amount + " Farming XP";
            case "GARDEN_XP" -> "+" + amount + " Garden XP";
            case "JACOBS_TICKET" -> "+" + amount + " Jacob's Ticket" + (amount == 1 ? "" : "s");
            case "BARN_SKIN_UNLOCK" -> capitalizeWords(reward.getKey()) + " Barn Skin";
            case "GREENHOUSE_UNLOCK" -> "Greenhouse Unlock";
            case "SKYMART_UNLOCK" -> "SkyMart Unlock: " + capitalizeWords(reward.getKey());
            case "POWDER" -> "+" + amount + " " + capitalizeWords(reward.getKey()) + " Powder";
            case "ESSENCE" -> "+" + amount + " " + capitalizeWords(reward.getKey()) + " Essence";
            case "PELTS" -> "+" + amount + " Pelt" + (amount == 1 ? "" : "s");
            case "FAIRY_SOUL" -> "+" + amount + " Fairy Soul";
            case "ACCESS_FLAG", "PROFILE_FLAG", "TUTORIAL_FLAG" -> capitalizeWords(reward.getKey());
            case "RANDOM_POOL" ->
                "Random " + capitalizeWords(reward.getPoolId().isBlank() ? reward.getKey() : reward.getPoolId());
            case "LEGACY" -> capitalizeWords(reward.getKey());
            default -> (amount > 0 ? "+" + amount + " " : "") + capitalizeWords(reward.getKey());
        };
    }

    public Map<String, Object> getVisitor(String id) {
        return visitors.stream()
            .filter(entry -> GardenConfigRegistry.getString(entry, "id", "").equalsIgnoreCase(id))
            .findFirst()
            .orElse(Map.of());
    }

    public String getEntityKind(Map<String, Object> visitor) {
        return normalizeItemKey(GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "kind", "HUMAN"));
    }

    public String getEntityProfession(Map<String, Object> visitor) {
        return normalizeItemKey(GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "profession", "NONE"));
    }

    public String getEntityType(Map<String, Object> visitor) {
        return normalizeItemKey(GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "type", "PLAINS"));
    }

    public String getEntityTexture(Map<String, Object> visitor) {
        return GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "texture", "");
    }

    public String getEntitySignature(Map<String, Object> visitor) {
        return GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "signature", "");
    }

    public String getIconItem(Map<String, Object> visitor) {
        return normalizeItemKey(GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "icon_item", ""));
    }

    public String getIconHeadTexture(Map<String, Object> visitor) {
        return GardenConfigRegistry.getString(GardenConfigRegistry.getSection(visitor, "entity"), "icon_head_texture", "");
    }

    private CompactionProfile chooseCompactionProfile(List<CompactionProfile> profiles) {
        double totalWeight = profiles.stream()
            .mapToDouble(profile -> Math.max(0D, profile.weight()))
            .sum();
        if (totalWeight <= 0D) {
            return profiles.getFirst();
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cursor = 0D;
        for (CompactionProfile profile : profiles) {
            cursor += Math.max(0D, profile.weight());
            if (roll <= cursor) {
                return profile;
            }
        }
        return profiles.getLast();
    }

    private String normalizeItemKey(String key) {
        if (key == null) {
            return "";
        }
        return key.trim().replace(' ', '_').toUpperCase();
    }

    private long parseLong(String[] tokens, int index, long defaultValue) {
        if (tokens.length <= index) {
            return defaultValue;
        }
        try {
            return Long.parseLong(tokens[index]);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public record CompactedRequest(String itemId, int amount, int rawAmount) {
    }

    public record VisitorRequirement(String type, String key, long amount, String secondaryKey, String display) {
    }

    private record CompactionProfile(String id, double weight, List<CompactionStage> stages) {
    }

    private record CompactionStage(String itemId, int rawCost) {
    }

    private GardenData.GardenRewardState parseRewardMap(Map<?, ?> rewardMapRaw) {
        Map<String, Object> rewardMap = castMap(rewardMapRaw);
        GardenData.GardenRewardState rewardState = new GardenData.GardenRewardState();
        rewardState.setType(normalizeItemKey(GardenConfigRegistry.getString(rewardMap, "type", "")));
        rewardState.setKey(normalizeItemKey(GardenConfigRegistry.getString(rewardMap, "key", "")));
        rewardState.setAmount(GardenConfigRegistry.getLong(rewardMap, "amount", 0L));
        rewardState.setMin(GardenConfigRegistry.getLong(rewardMap, "min", 0L));
        rewardState.setMax(GardenConfigRegistry.getLong(rewardMap, "max", 0L));
        rewardState.setPoolId(normalizeItemKey(GardenConfigRegistry.getString(rewardMap, "pool_id", "")));
        rewardState.setFirstVisitOnly(GardenConfigRegistry.getBoolean(rewardMap, "first_visit_only", false));
        rewardState.setDisplayOverride(GardenConfigRegistry.getString(rewardMap, "display", ""));

        Object metadataRaw = rewardMap.get("metadata");
        if (metadataRaw instanceof Map<?, ?> metadataMapRaw) {
            Map<String, String> metadata = new LinkedHashMap<>();
            metadataMapRaw.forEach((metadataKey, metadataValue) -> metadata.put(
                String.valueOf(metadataKey),
                String.valueOf(metadataValue)
            ));
            rewardState.setMetadata(metadata);
        }
        return rewardState;
    }

    private GardenData.GardenRewardState parseLegacyReward(String rawReward) {
        if (rawReward == null || rawReward.isBlank()) {
            return null;
        }

        String trimmed = rawReward.trim();
        boolean firstVisitOnly = trimmed.toUpperCase().endsWith("_ON_FIRST_VISIT");
        if (firstVisitOnly) {
            trimmed = trimmed.substring(0, trimmed.length() - "_ON_FIRST_VISIT".length());
        }

        String[] tokens = trimmed.split(":", 3);
        String head = normalizeItemKey(tokens[0]);
        GardenData.GardenRewardState rewardState = new GardenData.GardenRewardState();
        rewardState.setFirstVisitOnly(firstVisitOnly);

        if (tokens.length == 1) {
            if (ItemType.get(head) != null || "GREENHOUSE_BLUEPRINT".equals(head)) {
                rewardState.setType("ITEM");
                rewardState.setKey(head);
                rewardState.setAmount(1L);
                return rewardState;
            }
            if ("JACOBS_TICKET".equals(head)) {
                rewardState.setType("JACOBS_TICKET");
                rewardState.setKey(head);
                rewardState.setAmount(1L);
                return rewardState;
            }
            rewardState.setType("PROFILE_FLAG");
            rewardState.setKey(head);
            rewardState.setAmount(1L);
            return rewardState;
        }

        switch (head) {
            case "ITEM" -> {
                rewardState.setType("ITEM");
                rewardState.setKey(normalizeItemKey(tokens.length >= 2 ? tokens[1] : ""));
                rewardState.setAmount(parseLong(tokens, 2, 1L));
            }
            case "BITS", "COPPER", "FARMING_XP", "GARDEN_XP", "JACOBS_TICKET" -> {
                rewardState.setType(head);
                rewardState.setKey(head);
                rewardState.setAmount(parseLong(tokens, 1, "JACOBS_TICKET".equals(head) ? 1L : 0L));
            }
            case "BARN_SKIN" -> {
                rewardState.setType("BARN_SKIN_UNLOCK");
                rewardState.setKey(normalizeItemKey(tokens[1]));
                rewardState.setAmount(1L);
            }
            case "SKYMART_UNLOCK" -> {
                rewardState.setType("SKYMART_UNLOCK");
                rewardState.setKey(normalizeItemKey(tokens[1]));
                rewardState.setAmount(1L);
            }
            case "TUTORIAL_FLAG" -> {
                rewardState.setType("TUTORIAL_FLAG");
                rewardState.setKey(normalizeItemKey(tokens[1]));
                rewardState.setAmount(1L);
            }
            default -> {
                if (ItemType.get(head) != null) {
                    rewardState.setType("ITEM");
                    rewardState.setKey(head);
                    rewardState.setAmount(parseLong(tokens, 1, 1L));
                } else {
                    rewardState.setType("PROFILE_FLAG");
                    rewardState.setKey(normalizeItemKey(trimmed));
                    rewardState.setAmount(1L);
                }
            }
        }
        return rewardState;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> rawMap) {
        return (Map<String, Object>) rawMap;
    }

    private long parseLongValue(String rawValue, long defaultValue) {
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private int parseTrailingRoman(String requirement) {
        String roman = requirement.replaceAll("^.*\\s+", "").trim();
        return switch (roman.toUpperCase()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            case "XI" -> 11;
            case "XII" -> 12;
            case "XIII" -> 13;
            case "XIV" -> 14;
            case "XV" -> 15;
            default -> (int) parseLongValue(roman, 1L);
        };
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
            case 10 -> "X";
            case 11 -> "XI";
            case 12 -> "XII";
            case 13 -> "XIII";
            case 14 -> "XIV";
            case 15 -> "XV";
            default -> String.valueOf(value);
        };
    }

    private String capitalizeWords(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String[] parts = key.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[index].charAt(0)));
            if (parts[index].length() > 1) {
                builder.append(parts[index].substring(1));
            }
        }
        return builder.toString();
    }
}
