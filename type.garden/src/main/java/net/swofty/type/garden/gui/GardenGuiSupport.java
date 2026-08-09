package net.swofty.type.garden.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ChatColor;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.jacobscontest.GetJacobContestScheduleProtocol;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenBarnSkinDefinition;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.config.GardenPlotDefinition;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.garden.visitor.GardenBarnRuntime;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenComposter;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenCore;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenGreenhouse;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenPersonal;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenPests;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGardenVisitors;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GardenGuiSupport {
    private static final ProxyService JACOBS_CONTEST_SERVICE = new ProxyService(ServiceType.JACOBS_CONTEST);
    private static final Map<String, Material> FALLBACK_MATERIALS = Map.ofEntries(
        Map.entry("DEFAULT", Material.DARK_OAK_PLANKS),
        Map.entry("SPRUCE", Material.SPRUCE_LOG),
        Map.entry("RED", Material.QUARTZ_BLOCK),
        Map.entry("SUNNY", Material.RED_SANDSTONE),
        Map.entry("CABIN", Material.LIGHT_BLUE_TERRACOTTA),
        Map.entry("CHOCOLATE", Material.COCOA_BEANS),
        Map.entry("WHEAT", Material.WHEAT),
        Map.entry("CARROT", Material.CARROT),
        Map.entry("POTATO", Material.POTATO),
        Map.entry("PUMPKIN", Material.CARVED_PUMPKIN),
        Map.entry("SUGAR_CANE", Material.SUGAR_CANE),
        Map.entry("MELON_SLICE", Material.MELON_SLICE),
        Map.entry("CACTUS", Material.CACTUS),
        Map.entry("COCOA_BEANS", Material.COCOA_BEANS),
        Map.entry("MUSHROOM", Material.RED_MUSHROOM),
        Map.entry("NETHER_WART", Material.NETHER_WART),
        Map.entry("SUNFLOWER", Material.SUNFLOWER),
        Map.entry("MOONFLOWER", Material.BLUE_ORCHID),
        Map.entry("WILD_ROSE", Material.ROSE_BUSH)
    );

    private GardenGuiSupport() {
    }

    public static GardenData.GardenCoreData core(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_CORE, DatapointGardenCore.class)
            .getValue();
    }

    public static GardenData.GardenVisitorsData visitors(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_VISITORS, DatapointGardenVisitors.class)
            .getValue();
    }

    public static GardenData.GardenPestsData pests(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_PESTS, DatapointGardenPests.class)
            .getValue();
    }

    public static GardenData.GardenComposterData composter(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_COMPOSTER, DatapointGardenComposter.class)
            .getValue();
    }

    public static GardenData.GardenGreenhouseData greenhouse(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_GREENHOUSE, DatapointGardenGreenhouse.class)
            .getValue();
    }

    public static GardenData.GardenPersonalData personal(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.GARDEN_PERSONAL, DatapointGardenPersonal.class)
            .getValue();
    }

    public static SkyBlockGarden garden(SkyBlockPlayer player) {
        return player.getSkyBlockGarden() instanceof SkyBlockGarden garden ? garden : null;
    }

    public static ItemStack.Builder itemWithLore(String itemId, String displayName, int amount, List<String> lore) {
        ItemStack.Builder builder = baseItem(itemId, amount);
        builder.set(DataComponents.CUSTOM_NAME, Component.text(displayName).decoration(TextDecoration.ITALIC, false));
        return ItemStackCreator.updateLore(builder, lore);
    }

    public static ItemStack.Builder itemWithLore(String itemId, String displayName, List<String> lore) {
        return itemWithLore(itemId, displayName, 1, lore);
    }

    public static String colorForRarity(String rarity) {
        return switch (rarity == null ? "COMMON" : rarity.toUpperCase()) {
            case "UNCOMMON" -> "§a";
            case "RARE" -> "§9";
            case "EPIC" -> "§5";
            case "LEGENDARY" -> "§6";
            case "MYTHIC" -> "§d";
            case "SPECIAL" -> "§c";
            default -> "§f";
        };
    }

    public static String progressBar(double current, double max) {
        if (max <= 0D) {
            return "§8Progress threshold unavailable";
        }
        return StringUtility.createLineProgressBar(20, ChatColor.DARK_GREEN, current, max);
    }

    public static GardenData.GardenChipProgress getOrCreateChipProgress(SkyBlockPlayer player, String chipId) {
        return personal(player).getChips().computeIfAbsent(chipId, ignored -> new GardenData.GardenChipProgress());
    }

    public static boolean isBarnSkinUnlocked(SkyBlockPlayer player, GardenBarnSkinDefinition definition) {
        GardenData.GardenCoreData core = core(player);
        if (core.getOwnedBarnSkins().contains(definition.id())) {
            return true;
        }
        return GardenServices.levels().getLevels().stream().anyMatch(level ->
            GardenConfigRegistry.getInt(level, "level", 0) <= core.getLevel()
                && GardenConfigRegistry.getList(level, "barn_skin_unlocks").stream()
                .map(String::valueOf)
                .anyMatch(definition.id()::equalsIgnoreCase));
    }

    public static void syncComposter(SkyBlockPlayer player) {
        GardenData.GardenComposterData data = composter(player);
        long now = System.currentTimeMillis();
        if (data.getLastUpdatedAt() <= 0L) {
            data.setLastUpdatedAt(now);
            return;
        }

        int speedTier = data.getUpgrades().getOrDefault("speed", 0);
        int costReductionTier = data.getUpgrades().getOrDefault("cost_reduction", 0);
        long cycleMillis = Math.max(1L, Math.round(
            (GardenServices.composter().getBaseProductionSeconds() * 1000D)
                / GardenServices.composter().calculateSpeedMultiplier(speedTier)
        ));
        double matterCost = GardenServices.composter().calculateOrganicMatterCost(costReductionTier);
        double fuelCost = GardenServices.composter().calculateFuelCost(costReductionTier);

        long remaining = now - data.getLastUpdatedAt();
        while (remaining >= cycleMillis
            && data.getOrganicMatter() >= matterCost
            && data.getFuel() >= fuelCost) {
            data.setOrganicMatter(data.getOrganicMatter() - matterCost);
            data.setFuel(data.getFuel() - fuelCost);
            data.setCompostAvailable(data.getCompostAvailable() + 1);
            remaining -= cycleMillis;
        }
        data.setLastUpdatedAt(now - remaining);
    }

    public static boolean acceptVisitor(SkyBlockPlayer player, String visitorId) {
        GardenData.GardenVisitorsData visitors = visitors(player);
        Optional<GardenData.GardenVisitorState> visitorOptional = visitors.getActiveVisitors().stream()
            .filter(visitor -> visitor.getVisitorId().equalsIgnoreCase(visitorId))
            .findFirst();
        if (visitorOptional.isEmpty()) {
            return false;
        }

        GardenData.GardenVisitorState visitor = visitorOptional.get();
        Map<ItemType, Integer> requiredItems = new LinkedHashMap<>();
        for (GardenData.GardenRequest request : visitor.getRequests()) {
            ItemType type = ItemType.get(request.getItemId());
            if (type == null) {
                return false;
            }
            requiredItems.merge(type, request.getAmount(), Integer::sum);
        }

        if (!player.removeItemsFromPlayer(requiredItems)) {
            return false;
        }

        visitors.getActiveVisitors().remove(visitor);
        pushQueuedVisitor(visitors);
        visitors.getServedCounts().merge(visitorId, 1, Integer::sum);
        visitors.getLogbookEntries().add(visitorId);

        GardenData.GardenCoreData core = core(player);
        boolean firstUniqueVisit = core.getServedUniqueVisitors().add(visitorId);
        core.setExperience(core.getExperience() + visitor.getGardenXp());
        core.setCopper(core.getCopper() + visitor.getCopper());

        player.getSkills().increase(player, SkillCategories.FARMING, (double) visitor.getFarmingXp());
        DatapointInteger bits = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class);
        bits.setValue(bits.getValue() + visitor.getBits());

        for (GardenData.GardenRewardState reward : visitor.getGuaranteedRewards()) {
            giveReward(player, reward);
        }
        for (GardenData.GardenRewardState reward : visitor.getBonusRewards()) {
            giveReward(player, reward);
        }

        if (visitorId.equalsIgnoreCase("carpenter") && !greenhouse(player).isBlueprintUnlocked()) {
            greenhouse(player).setBlueprintUnlocked(true);
            giveReward(player, GardenServices.visitors().parseReward("GREENHOUSE_BLUEPRINT"));
        }

        if (firstUniqueVisit) {
            net.swofty.type.garden.GardenServices.milestones().advanceVisitorMilestone(player, "UNIQUE_SERVED", 1);
        }
        net.swofty.type.garden.GardenServices.milestones().advanceVisitorMilestone(player, "OFFERS_ACCEPTED", 1);

        player.playSuccessSound();
        GardenBarnRuntime.requestImmediateSync(player);
        return true;
    }

    public static boolean refuseVisitor(SkyBlockPlayer player, String visitorId) {
        GardenData.GardenVisitorsData visitors = visitors(player);
        boolean removed = visitors.getActiveVisitors().removeIf(visitor -> visitor.getVisitorId().equalsIgnoreCase(visitorId));
        if (removed) {
            pushQueuedVisitor(visitors);
            GardenBarnRuntime.requestImmediateSync(player);
        }
        return removed;
    }

    public static List<UpcomingContestDisplay> getUpcomingContests(int amount) {
        if (!JACOBS_CONTEST_SERVICE.isOnline().join()) {
            return List.of();
        }

        Object response = JACOBS_CONTEST_SERVICE.handleRequest(
            new GetJacobContestScheduleProtocol.GetJacobContestScheduleMessage(
                SkyBlockCalendar.getElapsed(),
                amount
            )
        ).join();

        if (!(response instanceof GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse contests)) {
            return List.of();
        }

        int year = contests.year();
        long previousStart = Long.MIN_VALUE;
        List<UpcomingContestDisplay> displayEntries = new ArrayList<>();
        for (GetJacobContestScheduleProtocol.ContestScheduleEntry entry : contests.entries()) {
            if (previousStart != Long.MIN_VALUE && entry.startTime() < previousStart) {
                year++;
            }
            displayEntries.add(new UpcomingContestDisplay(year, entry));
            previousStart = entry.startTime();
        }
        return displayEntries;
    }

    public static String formatContestDate(UpcomingContestDisplay display) {
        return SkyBlockCalendar.getMonthName(display.entry().startTime()) + " "
            + StringUtility.commaifyAndTh(SkyBlockCalendar.getDay(display.entry().startTime()));
    }

    public static String formatContestCrops(GetJacobContestScheduleProtocol.ContestScheduleEntry entry, int activeIndex) {
        List<String> crops = new ArrayList<>();
        for (int index = 0; index < entry.crops().size(); index++) {
            String prefix = entry.index() == activeIndex && index == entry.crops().size() - 1 ? "§6☘ " : "§e○ ";
            crops.add(prefix + "§7" + StringUtility.toNormalCase(entry.crops().get(index)));
        }
        return String.join("\n", crops);
    }

    public static int getMedalCount(SkyBlockPlayer player, String key) {
        return personal(player).getMedals().getOrDefault(key.toUpperCase(), 0);
    }

    public static List<GardenPlotDefinition> lockedPlotsFirst(SkyBlockPlayer player) {
        GardenData.GardenCoreData core = core(player);
        SkyBlockGarden garden = garden(player);
        if (garden == null) {
            return List.of();
        }
        return garden.getPlotService().getPlots().stream()
            .sorted(Comparator
                .comparing((GardenPlotDefinition plot) -> plot.defaultUnlocked() || core.getUnlockedPlots().contains(plot.id()))
                .thenComparing(GardenPlotDefinition::id))
            .toList();
    }

    private static ItemStack.Builder baseItem(String itemId, int amount) {
        ItemType itemType = ItemType.get(itemId);
        if (itemType != null) {
            ItemStack.Builder builder = new SkyBlockItem(itemType).getDisplayItem().amount(amount);
            return builder;
        }
        return ItemStack.builder(FALLBACK_MATERIALS.getOrDefault(itemId.toUpperCase(), Material.BOOK)).amount(amount);
    }

    private static void pushQueuedVisitor(GardenData.GardenVisitorsData visitors) {
        if (visitors.getActiveVisitors().size() >= 5 || visitors.getQueuedVisitors().isEmpty()) {
            return;
        }
        GardenData.GardenVisitorState queued = visitors.getQueuedVisitors().removeFirst();
        queued.setQueued(false);
        visitors.getActiveVisitors().add(queued);
    }

    public static String describeReward(GardenData.GardenRewardState reward) {
        return GardenServices.visitors().describeReward(reward);
    }

    public static List<String> describeRewards(List<GardenData.GardenRewardState> rewards, String colorPrefix) {
        List<String> lore = new ArrayList<>();
        for (GardenData.GardenRewardState reward : rewards) {
            String description = describeReward(reward);
            if (!description.isBlank()) {
                lore.add(colorPrefix + description);
            }
        }
        return lore;
    }

    public static ItemStack.Builder visitorIcon(Map<String, Object> definition, String displayName, String rarity, List<String> lore) {
        String iconHeadTexture = GardenServices.visitors().getIconHeadTexture(definition);
        if (!iconHeadTexture.isBlank()) {
            return ItemStackCreator.getStackHead(
                colorForRarity(rarity) + displayName,
                iconHeadTexture,
                1,
                lore.toArray(String[]::new)
            );
        }

        String iconItem = GardenServices.visitors().getIconItem(definition);
        if (!iconItem.isBlank()) {
            return itemWithLore(iconItem, colorForRarity(rarity) + displayName, lore);
        }

        if ("VILLAGER".equals(GardenServices.visitors().getEntityKind(definition))) {
            return ItemStackCreator.getStack(
                colorForRarity(rarity) + displayName,
                Material.VILLAGER_SPAWN_EGG,
                1,
                lore
            );
        }

        return itemWithLore("BOOK", colorForRarity(rarity) + displayName, lore);
    }

    private static void giveReward(SkyBlockPlayer player, GardenData.GardenRewardState reward) {
        if (reward == null) {
            return;
        }
        long amount = normalizeRewardAmount(reward);
        switch (reward.getType()) {
            case "ITEM" -> {
                ItemType itemType = ItemType.get(reward.getKey());
                if (itemType != null) {
                    player.addAndUpdateItem(itemType, (int) Math.max(1L, amount));
                }
            }
            case "BITS" -> {
                DatapointInteger bits = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS, DatapointInteger.class);
                bits.setValue(bits.getValue() + (int) amount);
            }
            case "COPPER" -> core(player).setCopper(core(player).getCopper() + amount);
            case "FARMING_XP" -> player.getSkills().increase(player, SkillCategories.FARMING, (double) amount);
            case "GARDEN_XP" -> core(player).setExperience(core(player).getExperience() + amount);
            case "JACOBS_TICKET" ->
                personal(player).setJacobsTickets(personal(player).getJacobsTickets() + (int) amount);
            case "BARN_SKIN_UNLOCK" -> core(player).getOwnedBarnSkins().add(reward.getKey().toLowerCase());
            case "GREENHOUSE_UNLOCK" -> greenhouse(player).setBlueprintUnlocked(true);
            case "SKYMART_UNLOCK" -> core(player).getSkyMartPurchases().add(reward.getKey().toLowerCase());
            case "TUTORIAL_FLAG" -> personal(player).getTutorialFlags().add(reward.getKey().toLowerCase());
            case "PROFILE_FLAG", "ACCESS_FLAG" ->
                personal(player).getVisitorRequirementFlags().add(reward.getKey().toLowerCase());
            case "PROFILE_COUNTER" ->
                personal(player).getVisitorRequirementCounters().merge(reward.getKey().toLowerCase(), amount, Long::sum);
            case "POWDER" ->
                personal(player).getVisitorRequirementCounters().merge(("powder_" + reward.getKey()).toLowerCase(), amount, Long::sum);
            case "ESSENCE" ->
                personal(player).getVisitorRequirementCounters().merge(("essence_" + reward.getKey()).toLowerCase(), amount, Long::sum);
            case "PELTS" -> personal(player).getVisitorRequirementCounters().merge("pelts", amount, Long::sum);
            case "FAIRY_SOUL" ->
                personal(player).getVisitorRequirementCounters().merge("fairy_souls", amount, Long::sum);
            default -> {
            }
        }
    }

    private static long normalizeRewardAmount(GardenData.GardenRewardState reward) {
        if (reward.getAmount() > 0) {
            return reward.getAmount();
        }
        if (reward.getMin() > 0 && reward.getMax() >= reward.getMin()) {
            return reward.getMax() == reward.getMin()
                ? reward.getMin()
                : java.util.concurrent.ThreadLocalRandom.current().nextLong(reward.getMin(), reward.getMax() + 1);
        }
        return 1L;
    }

    public record UpcomingContestDisplay(int year, GetJacobContestScheduleProtocol.ContestScheduleEntry entry) {
    }
}
