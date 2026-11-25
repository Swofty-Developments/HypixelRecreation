package net.swofty.type.garden.visitor;

import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.type.garden.GardenCropRegistry;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class GardenVisitorRuntime {
    private static final Pattern ANY_PATTERN = Pattern.compile("^ANY(?:\\s+(\\d+))?$", Pattern.CASE_INSENSITIVE);
    private static final List<String> RARITY_ORDER = List.of("UNCOMMON", "RARE", "LEGENDARY", "MYTHIC", "SPECIAL");
    private GardenVisitorRuntime() {
    }

    public static void start(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            tickAllGardens();
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_END);
    }

    public static GardenData.GardenVisitorState createVisitorState(SkyBlockPlayer player, String visitorId) {
        if (player == null) {
            return null;
        }
        Map<String, Object> definition = GardenServices.visitors().getVisitor(visitorId);
        if (definition.isEmpty()) {
            return null;
        }
        return buildVisitorState(
            player,
            GardenGuiSupport.visitors(player),
            GardenGuiSupport.core(player),
            definition,
            System.currentTimeMillis()
        );
    }

    private static void tickAllGardens() {
        Map<UUID, List<SkyBlockPlayer>> playersByProfile = SkyBlockGenericLoader.getLoadedPlayers().stream()
            .filter(SkyBlockPlayer::isOnGarden)
            .filter(player -> player.getSkyblockDataHandler() != null)
            .collect(Collectors.groupingBy(player -> player.getSkyblockDataHandler().getCurrentProfileId()));

        playersByProfile.values().forEach(GardenVisitorRuntime::tickProfile);
    }

    private static void tickProfile(List<SkyBlockPlayer> viewers) {
        if (viewers.isEmpty()) {
            return;
        }

        SkyBlockPlayer primary = viewers.getFirst();
        GardenData.GardenVisitorsData visitors = GardenGuiSupport.visitors(primary);
        GardenData.GardenCoreData core = GardenGuiSupport.core(primary);
        long now = System.currentTimeMillis();

        if (visitors.getLastProcessedAt() <= 0L) {
            visitors.setLastProcessedAt(now);
        }
        if (visitors.getNextArrivalAt() <= 0L) {
            visitors.setNextArrivalAt(now + arrivalMillis(core));
        }

        long realElapsed = Math.max(0L, now - visitors.getLastProcessedAt());
        if (realElapsed > 0L && isActivelyFarming(visitors, now)) {
            int farmingSpeedup = Math.max(1, GardenServices.visitors().getFarmingSpeedupMultiplier());
            long bonusReduction = realElapsed * (farmingSpeedup - 1L);
            visitors.setNextArrivalAt(visitors.getNextArrivalAt() - bonusReduction);
        }

        int maxCapacity = GardenServices.visitors().getMaxVisibleVisitors() + GardenServices.visitors().getMaxQueuedVisitors();
        while (visitors.getActiveVisitors().size() + visitors.getQueuedVisitors().size() < maxCapacity
            && now >= visitors.getNextArrivalAt()) {
            if (!spawnNextVisitor(primary, viewers, visitors, core, now)) {
                visitors.setNextArrivalAt(now + arrivalMillis(core));
                break;
            }
            visitors.setNextArrivalAt(visitors.getNextArrivalAt() + arrivalMillis(core));
        }

        visitors.setLastProcessedAt(now);
        core.setLastActiveAt(now);
    }

    private static boolean spawnNextVisitor(
        SkyBlockPlayer primary,
        List<SkyBlockPlayer> viewers,
        GardenData.GardenVisitorsData visitors,
        GardenData.GardenCoreData core,
        long now
    ) {
        List<Map<String, Object>> candidates = GardenServices.visitors().getVisitors().stream()
            .filter(definition -> canVisit(primary, visitors, core, definition))
            .toList();
        if (candidates.isEmpty()) {
            return false;
        }

        Map<String, Object> chosen = chooseWeighted(candidates);
        GardenData.GardenVisitorState state = buildVisitorState(primary, visitors, core, chosen, now);

        if (visitors.getActiveVisitors().size() < GardenServices.visitors().getMaxVisibleVisitors()) {
            visitors.getActiveVisitors().add(state);
        } else {
            state.setQueued(true);
            visitors.getQueuedVisitors().add(state);
        }

        String visitorId = state.getVisitorId();
        visitors.getVisitCounts().merge(visitorId, 1, Integer::sum);
        visitors.getLogbookEntries().add(visitorId);

        String displayName = GardenConfigRegistry.getString(chosen, "display_name", StringUtility.toNormalCase(visitorId));
        viewers.forEach(player -> player.sendMessage("§aVisitor §e" + displayName + " §ahas arrived at your Desk."));
        GardenBarnRuntime.requestImmediateSync(primary);
        return true;
    }

    private static GardenData.GardenVisitorState buildVisitorState(
        SkyBlockPlayer player,
        GardenData.GardenVisitorsData visitors,
        GardenData.GardenCoreData core,
        Map<String, Object> definition,
        long now
    ) {
        String visitorId = GardenConfigRegistry.getString(definition, "id", "");
        String rarity = GardenConfigRegistry.getString(definition, "rarity", "UNCOMMON").toUpperCase();
        boolean firstVisit = !visitors.getVisitCounts().containsKey(visitorId) && !visitors.getServedCounts().containsKey(visitorId);

        int[] baseRange = GardenServices.visitors().getBaseItemsRange(core.getLevel());
        int baseItems = ThreadLocalRandom.current().nextInt(baseRange[0], baseRange[1] + 1);

        List<WantedRequest> wantedItems = resolveWantedItems(definition, firstVisit);
        List<GardenData.GardenRequest> requests = new ArrayList<>();
        double farmingXp = 0D;
        int copper = 0;

        for (WantedRequest wantedItem : wantedItems) {
            double quantityMultiplier = GardenServices.visitors().getQuantityMultiplier(wantedItem.itemId());
            int rawAmount = wantedItem.fixedAmount() > 0
                ? wantedItem.fixedAmount()
                : Math.max(1, (int) Math.round(
                baseItems * quantityMultiplier * GardenServices.visitors().getRequestMultiplier(rarity)
            ));
            GardenVisitorService.CompactedRequest compactedRequest = GardenServices.visitors().compactRequest(wantedItem.itemId(), rawAmount);

            GardenData.GardenRequest request = new GardenData.GardenRequest();
            request.setItemId(compactedRequest.itemId());
            request.setAmount(compactedRequest.amount());
            request.setItemQuantityMultiplier(quantityMultiplier);
            requests.add(request);

            farmingXp += GardenServices.visitors().calculateFarmingXp(
                baseItems,
                GardenServices.visitors().getItemFarmingXpAmount(wantedItem.itemId()),
                rarity
            );
            copper += GardenServices.visitors().calculateCopper(baseItems, quantityMultiplier, rarity);
        }

        long farmingXpReward = Math.round(farmingXp);
        int gardenXpReward = GardenServices.visitors().calculateGardenXp(core.getLevel(), rarity);
        int bits = GardenServices.visitors().getBitsBase();

        Map<String, Object> firstVisitOverride = GardenConfigRegistry.getSection(definition, "first_visit_override");
        if (firstVisit && !firstVisitOverride.isEmpty()) {
            farmingXpReward = GardenConfigRegistry.getLong(firstVisitOverride, "farming_xp", farmingXpReward);
            copper = GardenConfigRegistry.getInt(firstVisitOverride, "copper", copper);
            gardenXpReward = GardenConfigRegistry.getInt(firstVisitOverride, "garden_xp", gardenXpReward);
            bits = GardenConfigRegistry.getInt(firstVisitOverride, "bits", bits);
        }

        Map<String, Object> overrideRewards = GardenConfigRegistry.getSection(definition, "override_rewards");
        List<GardenData.GardenRewardState> guaranteedRewards = new ArrayList<>();
        if (!overrideRewards.isEmpty()) {
            farmingXpReward = GardenConfigRegistry.getLong(overrideRewards, "farming_xp", farmingXpReward);
            copper = GardenConfigRegistry.getInt(overrideRewards, "copper", copper);
            gardenXpReward = GardenConfigRegistry.getInt(overrideRewards, "garden_xp", gardenXpReward);
            bits = GardenConfigRegistry.getInt(overrideRewards, "bits", bits);
            guaranteedRewards.addAll(GardenServices.visitors().getConfiguredRewards(overrideRewards, "rewards"));
            for (Object reward : GardenConfigRegistry.getList(overrideRewards, "items")) {
                GardenData.GardenRewardState rewardState = GardenServices.visitors().parseReward(reward);
                if (rewardState != null) {
                    guaranteedRewards.add(rewardState);
                }
            }
        }

        guaranteedRewards.addAll(GardenServices.visitors().getConfiguredRewards(definition, "guaranteed_rewards"));

        for (Object reward : GardenConfigRegistry.getList(definition, "unique_rewards")) {
            GardenData.GardenRewardState rewardState = GardenServices.visitors().parseReward(reward);
            if (rewardState == null) {
                continue;
            }
            if (!rewardState.isFirstVisitOnly() || firstVisit) {
                guaranteedRewards.add(rewardState);
            }
        }

        if (firstVisit) {
            guaranteedRewards.addAll(GardenServices.visitors().getConfiguredRewards(firstVisitOverride, "rewards"));
        }

        List<GardenData.GardenRewardState> bonusRewards = rollBonusRewards(rarity);

        GardenData.GardenVisitorState state = new GardenData.GardenVisitorState();
        state.setVisitorId(visitorId);
        state.setRarity(rarity);
        state.setRequests(requests);
        state.setFarmingXp(farmingXpReward);
        state.setGardenXp(gardenXpReward);
        state.setCopper(copper);
        state.setBits(bits);
        state.setGuaranteedRewards(guaranteedRewards);
        state.setBonusRewards(bonusRewards);
        state.setArrivedAt(now);
        return state;
    }

    private static boolean canVisit(
        SkyBlockPlayer player,
        GardenData.GardenVisitorsData visitors,
        GardenData.GardenCoreData core,
        Map<String, Object> definition
    ) {
        String visitorId = GardenConfigRegistry.getString(definition, "id", "");
        if (visitorId.isBlank()) {
            return false;
        }
        boolean alreadyPresent = visitors.getActiveVisitors().stream().anyMatch(visitor -> visitor.getVisitorId().equalsIgnoreCase(visitorId))
            || visitors.getQueuedVisitors().stream().anyMatch(visitor -> visitor.getVisitorId().equalsIgnoreCase(visitorId));
        if (alreadyPresent) {
            return false;
        }
        if (core.getLevel() < GardenConfigRegistry.getInt(definition, "garden_level", 1)) {
            return false;
        }

        for (GardenVisitorService.VisitorRequirement requirement : GardenServices.visitors().getRequirements(definition)) {
            if (!isRequirementMet(player, core, visitorId, requirement)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRequirementMet(SkyBlockPlayer player, GardenData.GardenCoreData core, String visitorId, GardenVisitorService.VisitorRequirement requirement) {
        if (requirement == null) {
            return true;
        }
        return switch (requirement.type()) {
            case "SPOKEN_TO_NPC" -> isTalkRequirementMet(player, visitorId, requirement.key());
            case "GARDEN_LEVEL_AT_LEAST" -> core.getLevel() >= requirement.amount();
            case "SKILL_LEVEL_AT_LEAST" -> switch (requirement.key()) {
                case "FARMING" -> player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= requirement.amount();
                case "FISHING" -> player.getSkills().getCurrentLevel(SkillCategories.FISHING) >= requirement.amount();
                default -> false;
            };
            case "GARDEN_FLAG", "PROFILE_FLAG", "CUSTOM_PROFILE_FLAG", "ACCESS_FLAG" ->
                GardenGuiSupport.personal(player).getVisitorRequirementFlags().contains(requirement.key())
                    || GardenGuiSupport.personal(player).getAccessFlags().contains(requirement.key())
                    || GardenGuiSupport.core(player).getVisitorRequirementFlags().contains(requirement.key());
            case "GARDEN_COUNTER_AT_LEAST", "PROFILE_COUNTER" ->
                ("SERVED_UNIQUE_VISITORS".equals(requirement.key()) && GardenGuiSupport.core(player).getServedUniqueVisitors().size() >= requirement.amount())
                    || GardenGuiSupport.personal(player).getVisitorRequirementCounters().getOrDefault(requirement.key(), 0L) >= requirement.amount()
                    || GardenGuiSupport.core(player).getVisitorRequirementCounters().getOrDefault(requirement.key(), 0L) >= requirement.amount();
            case "ITEM_DONATED" -> GardenGuiSupport.personal(player).getDonatedItems().contains(requirement.key());
            case "ITEM_EXPORTED" ->
                GardenGuiSupport.personal(player).getExportedItems().getOrDefault(requirement.key(), 0L) >= requirement.amount();
            case "HAS_UNLOCK" -> GardenGuiSupport.personal(player).getAccessFlags().contains(requirement.key())
                || GardenGuiSupport.core(player).getSkyMartPurchases().contains(requirement.key().toLowerCase())
                || GardenGuiSupport.core(player).getOwnedBarnSkins().contains(requirement.key().toLowerCase());
            case "VISITOR_SERVED" ->
                GardenGuiSupport.core(player).getServedUniqueVisitors().contains(requirement.key().toLowerCase())
                    || GardenGuiSupport.visitors(player).getServedCounts().getOrDefault(requirement.key().toLowerCase(), 0) >= requirement.amount();
            case "MAYOR_PERK_ACTIVE" -> GardenGuiSupport.personal(player).getAccessFlags().contains(requirement.key());
            default -> false;
        };
    }

    private static boolean isTalkRequirementMet(SkyBlockPlayer player, String visitorId, String requirementKey) {
        String normalizedNpc = normalizeDialogueFlag(requirementKey);
        List<String> spokenFlags = new ArrayList<>(GardenGuiSupport.personal(player).getSpokenNpcFlags());
        return spokenFlags.contains(visitorId.toLowerCase())
            || spokenFlags.contains(normalizedNpc)
            || spokenFlags.contains(requirementKey)
            || spokenFlags.contains(requirementKey.toLowerCase(java.util.Locale.ROOT))
            || spokenFlags.contains(requirementKey.toUpperCase(java.util.Locale.ROOT));
    }

    private static List<WantedRequest> resolveWantedItems(Map<String, Object> definition, boolean firstVisit) {
        Map<String, Object> firstVisitOverride = firstVisit ? GardenConfigRegistry.getSection(definition, "first_visit_override") : Map.of();
        List<Object> configuredWantedItems = firstVisit ? GardenConfigRegistry.getList(firstVisitOverride, "wanted_items") : List.of();
        if (configuredWantedItems.isEmpty()) {
            Map<String, Object> requestRules = GardenConfigRegistry.getSection(definition, "request_rules");
            configuredWantedItems = GardenConfigRegistry.getList(requestRules, "items");
        }
        if (configuredWantedItems.isEmpty()) {
            configuredWantedItems = GardenConfigRegistry.getList(definition, "wanted_items");
        }
        if (configuredWantedItems.isEmpty()) {
            return List.of(new WantedRequest("WHEAT", 0));
        }

        List<WantedRequest> resolved = new ArrayList<>();
        for (Object configuredWantedItem : configuredWantedItems) {
            if (configuredWantedItem instanceof Map<?, ?> mapRaw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemConfig = (Map<String, Object>) mapRaw;
                String itemId = GardenConfigRegistry.getString(itemConfig, "id", "").trim().toUpperCase();
                int fixedAmount = GardenConfigRegistry.getInt(itemConfig, "fixed_amount", 0);
                if (!itemId.isBlank()) {
                    resolved.add(new WantedRequest(itemId, Math.max(0, fixedAmount)));
                }
                continue;
            }

            String entry = String.valueOf(configuredWantedItem).trim();
            if (entry.isBlank()) {
                continue;
            }
            Matcher matcher = ANY_PATTERN.matcher(entry);
            if (matcher.matches()) {
                int amount = matcher.group(1) == null ? 1 : Integer.parseInt(matcher.group(1));
                List<String> pool = new ArrayList<>(GardenCropRegistry.getVisitorCropPool());
                while (amount > 0 && !pool.isEmpty()) {
                    String selected = pool.remove(ThreadLocalRandom.current().nextInt(pool.size()));
                    resolved.add(new WantedRequest(selected, 0));
                    amount--;
                }
                continue;
            }

            Matcher fixedMatcher = Pattern.compile("^([\\d,]+)X\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(entry);
            if (fixedMatcher.matches()) {
                int fixedAmount = Integer.parseInt(fixedMatcher.group(1).replace(",", ""));
                resolved.add(new WantedRequest(fixedMatcher.group(2).trim().toUpperCase(), fixedAmount));
                continue;
            }

            resolved.add(new WantedRequest(entry.toUpperCase(), 0));
        }
        if (resolved.isEmpty()) {
            return List.of(new WantedRequest("WHEAT", 0));
        }
        if (firstVisit && "jerry".equalsIgnoreCase(GardenConfigRegistry.getString(definition, "id", ""))
            && resolved.stream().anyMatch(request -> request.itemId().equals("BREAD"))) {
            return List.of(new WantedRequest("BREAD", 0));
        }
        return resolved;
    }

    private static List<GardenData.GardenRewardState> rollBonusRewards(String rarity) {
        List<GardenData.GardenRewardState> rewards = new ArrayList<>();
        for (Map<String, Object> entry : GardenServices.visitors().getBonusRewards()) {
            String minRarity = GardenConfigRegistry.getString(entry, "min_rarity", "UNCOMMON");
            if (!isAtLeastRarity(rarity, minRarity)) {
                continue;
            }
            if (ThreadLocalRandom.current().nextDouble() <= GardenConfigRegistry.getDouble(entry, "chance", 0D)) {
                List<GardenData.GardenRewardState> configuredRewards = GardenServices.visitors().getConfiguredRewards(entry, "rewards");
                if (!configuredRewards.isEmpty()) {
                    rewards.addAll(configuredRewards);
                    continue;
                }
                GardenData.GardenRewardState rewardState = GardenServices.visitors().parseReward(GardenConfigRegistry.getString(entry, "item", ""));
                if (rewardState != null) {
                    rewards.add(rewardState);
                }
            }
        }
        return rewards;
    }

    private static Map<String, Object> chooseWeighted(List<Map<String, Object>> candidates) {
        double totalWeight = candidates.stream()
            .mapToDouble(candidate -> GardenServices.visitors().getRarityWeight(GardenConfigRegistry.getString(candidate, "rarity", "UNCOMMON")))
            .sum();
        if (totalWeight <= 0D) {
            return candidates.getFirst();
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cursor = 0D;
        for (Map<String, Object> candidate : candidates) {
            cursor += GardenServices.visitors().getRarityWeight(GardenConfigRegistry.getString(candidate, "rarity", "UNCOMMON"));
            if (roll <= cursor) {
                return candidate;
            }
        }
        return candidates.getLast();
    }

    private static boolean isAtLeastRarity(String rarity, String minRarity) {
        return RARITY_ORDER.indexOf(rarity.toUpperCase()) >= RARITY_ORDER.indexOf(minRarity.toUpperCase());
    }

    private static boolean isActivelyFarming(GardenData.GardenVisitorsData visitors, long now) {
        return now - visitors.getLastFarmingActivityAt() <= 2_500L;
    }

    private static long arrivalMillis(GardenData.GardenCoreData core) {
        return GardenServices.visitors().getArrivalMinutes(core.getServedUniqueVisitors().size()) * 60_000L;
    }

    private static String normalizeDialogueFlag(String value) {
        return value.toLowerCase()
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private record WantedRequest(String itemId, int fixedAmount) {
    }
}
