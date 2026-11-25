package net.swofty.type.skyblockgeneric.fishing.resolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.swofty.commons.loot.LootEntry;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.loot.LootRoll;
import net.swofty.commons.loot.LootTable;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.catches.TrophyTier;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingTableDefinition;
import net.swofty.type.skyblockgeneric.fishing.registry.SeaCreatureDefinition;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodPartService;
import net.swofty.type.skyblockgeneric.fishing.tag.FishingTag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingLootResolver {

    private static final CatchPayload DEFAULT_CATCH =
        new CatchPayload.Item("RAW_FISH", 1, 5.0D, false);

    public static CatchPayload resolve(FishingContext context) {
        Optional<CatchPayload> questCatch = tryResolveQuestCatch(context);
        if (questCatch.isPresent()) {
            return questCatch.get();
        }

        Optional<CatchPayload> seaCreature = tryResolveSeaCreature(context);
        Optional<CatchPayload> trophyFish = tryResolveTrophyFish(context);
        if (seaCreature.isPresent() && trophyFish.isPresent()) {
            return new CatchPayload.Multi(List.of(seaCreature.get(), trophyFish.get()));
        }
        if (seaCreature.isPresent()) {
            return seaCreature.get();
        }
        if (trophyFish.isPresent()) {
            return trophyFish.get();
        }
        return resolveItem(context);
    }

    private static Optional<CatchPayload> tryResolveTrophyFish(FishingContext context) {
        if (context.medium() != FishingMedium.LAVA) {
            return Optional.empty();
        }

        double bonus = getTotalStatistic(context, ItemStatistic.TROPHY_FISH_CHANCE);
        if (context.bait() != null) {
            bonus += context.bait().getTrophyFishChanceBonus();
        }

        List<TrophyFishDefinition> eligible = new ArrayList<>();
        for (TrophyFishDefinition definition : FishingRegistry.getTrophyFish()) {
            if (!isEligibleForTrophyFish(context, definition)) {
                continue;
            }
            eligible.add(definition);
        }

        eligible.sort(Comparator.comparingDouble(TrophyFishDefinition::catchChance).thenComparing(TrophyFishDefinition::id));
        double trophyBonus = bonus;
        List<LootEntry<FishingContext, TrophyFishDefinition>> entries = eligible.stream()
                .map(definition -> new LootEntry<FishingContext, TrophyFishDefinition>(key("trophy/" + definition.id()), definition,
                        effectiveTrophyChance(context, definition, trophyBonus) / 100D))
                .toList();
        Optional<TrophyFishDefinition> selected = new LootTable<FishingContext, TrophyFishDefinition>(
                key("trophy_fish"), List.of(new LootPool<>(key("species"), LootPool.Mode.FIRST_SUCCESS, entries)))
                .roll(context).stream().map(LootRoll::value).findFirst();
        if (selected.isEmpty()) return Optional.empty();

        TrophyFishDefinition definition = selected.get();
        TrophyTier tier = rollTrophyTier(context, definition);
        String itemId = switch (tier) {
            case DIAMOND -> definition.diamondItemId();
            case GOLD -> definition.goldItemId();
            case SILVER -> definition.silverItemId();
            case BRONZE -> definition.bronzeItemId();
        };
        return itemId == null ? Optional.empty()
                : Optional.of(new CatchPayload.TrophyFish(definition.id(), tier, itemId, 300.0D));
    }

    private static boolean isEligibleForTrophyFish(FishingContext context, TrophyFishDefinition definition) {
        if (!definition.regions().isEmpty() && (context.regionId() == null || !definition.regions().contains(context.regionId()))) {
            return false;
        }
        if (context.player().getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.FISHING) < definition.requiredFishingLevel()) {
            return false;
        }
        if (context.castDurationMs() < definition.minimumCastTimeMs()) {
            return false;
        }
        if (definition.requiredRodId() != null && !definition.requiredRodId().equals(context.rod().getAttributeHandler().getPotentialType().name())) {
            return false;
        }
        if (definition.requiredBaitId() != null && (context.bait() == null || !definition.requiredBaitId().equals(context.bait().getItemId()))) {
            return false;
        }
        if (definition.requiresStarterRodWithoutEnchantments()
            && (!"STARTER_LAVA_ROD".equals(context.rod().getAttributeHandler().getPotentialType().name())
            || context.rod().getAttributeHandler().getEnchantments().findAny().isPresent())) {
            return false;
        }
        if (definition.minimumMana() != null && context.player().getMaxMana() < definition.minimumMana()) {
            return false;
        }
        if (definition.minimumBobberDepth() != null && context.player().getPosition().y() - context.hookPosition().y() < definition.minimumBobberDepth()) {
            return false;
        }
        if (definition.maximumPlayerDistance() != null && context.player().getPosition().distance(context.hookPosition()) > definition.maximumPlayerDistance()) {
            return false;
        }
        return true;
    }

    private static double effectiveTrophyChance(FishingContext context, TrophyFishDefinition definition, double bonus) {
        if (!definition.specialGoldenFish()) {
            if ("MANA_RAY".equals(definition.id())) {
                return (context.player().getMaxMana() / 1000.0D) * (1.0D + bonus / 100.0D);
            }
            return definition.catchChance() * (1.0D + bonus / 100.0D);
        }
        long duration = context.castDurationMs();
        if (duration < 480_000L) {
            return 0.0D;
        }
        return Math.min(100.0D, ((duration - 480_000L) / 240_000.0D) * 100.0D);
    }

    private static Optional<CatchPayload> tryResolveQuestCatch(FishingContext context) {
        if (context.medium() != FishingMedium.WATER) return Optional.empty();
        if (context.regionId() == null) return Optional.empty();
        if (!"FISHING_OUTPOST".equals(context.regionId()) && !"FISHERMANS_HUT".equals(context.regionId())) {
            return Optional.empty();
        }
        if (!context.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISHERWOMAN_ENID)) {
            return Optional.empty();
        }
        if (context.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_UNLOCKED_SHIP)) {
            return Optional.empty();
        }
        if (context.player().getShipState().getEngine() != null) return Optional.empty();
        if (context.player().countItem(ItemType.RUSTY_SHIP_ENGINE) > 0) return Optional.empty();
        if (!rollChance("quest_engine", 1.0D)) return Optional.empty();

        return Optional.of(new CatchPayload.Quest(
            ItemType.RUSTY_SHIP_ENGINE.name(),
            1,
            15.0D,
            "You fished up a Rusty Ship Engine!"
        ));
    }

    private static TrophyTier rollTrophyTier(FishingContext context, TrophyFishDefinition definition) {
        var progress = context.player().getTrophyFishData().getProgress(definition.id());
        if (progress.getTotalCatches() + 1 >= 600 && !progress.hasTier(TrophyTier.DIAMOND.name())) {
            return TrophyTier.DIAMOND;
        }
        if (progress.getTotalCatches() + 1 >= 100 && !progress.hasTier(TrophyTier.GOLD.name())) {
            return TrophyTier.GOLD;
        }

        double charmBonus = 0.0D;
        var charm = context.rod().getAttributeHandler().getEnchantment(net.swofty.type.skyblockgeneric.enchantment.EnchantmentType.CHARM);
        if (charm != null) {
            charmBonus = charm.level() * 2.0D;
        }

        double multiplier = 1 + charmBonus / 100D;
        List<LootEntry<FishingContext, TrophyTier>> entries = List.of(
                new LootEntry<>(key("tier/diamond"), TrophyTier.DIAMOND, 0.002D * multiplier),
                new LootEntry<>(key("tier/gold"), TrophyTier.GOLD, 0.02D * multiplier),
                new LootEntry<>(key("tier/silver"), TrophyTier.SILVER, 0.25D * multiplier),
                new LootEntry<>(key("tier/bronze"), TrophyTier.BRONZE, 1)
        );
        return new LootTable<FishingContext, TrophyTier>(key("trophy_tier"), List.of(
                new LootPool<>(key("tier"), LootPool.Mode.FIRST_SUCCESS, entries)
        )).roll(context).getFirst().value();
    }

    private static Optional<CatchPayload> tryResolveSeaCreature(FishingContext context) {
        Optional<FishingTableDefinition> tableOpt = findTable(context);
        if (tableOpt.isEmpty()) return Optional.empty();
        FishingTableDefinition table = tableOpt.get();

        double seaCreatureChance = getTotalStatistic(context, ItemStatistic.SEA_CREATURE_CHANCE);
        if (context.hook() != null && context.hook().isTreasureOnly()) {
            return Optional.empty();
        }

        List<LootEntry<FishingContext, SeaCreatureDefinition>> entries = new ArrayList<>();
        for (FishingTableDefinition.SeaCreatureRoll roll : table.seaCreatures()) {
            SeaCreatureDefinition definition = FishingRegistry.getSeaCreature(roll.seaCreatureId());
            if (definition != null && context.player().getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.FISHING) < definition.requiredFishingLevel()) {
                continue;
            }
            if (definition != null && !definition.isAvailable(context)) {
                continue;
            }
            double tagBonus = definition == null ? 0.0D : getTagBonus(context, definition.tags());
            if (definition == null) continue;
            entries.add(new LootEntry<>(key("sea_creature/" + roll.seaCreatureId()), definition,
                    Math.min(100, roll.chance() + seaCreatureChance + tagBonus) / 100D));
        }
        Optional<SeaCreatureDefinition> selected = new LootTable<FishingContext, SeaCreatureDefinition>(
                key("sea_creatures"), List.of(new LootPool<>(key("creatures"), LootPool.Mode.FIRST_SUCCESS, entries)))
                .roll(context).stream().map(LootRoll::value).findFirst();
        if (selected.isEmpty()) return Optional.empty();
        SeaCreatureDefinition definition = selected.get();
        CatchPayload.SeaCreature payload = new CatchPayload.SeaCreature(definition.id(), definition.skillXp());
        return Optional.of(rollDoubleHook(context) ? payload.withDoubleHook() : payload);
    }

    private static CatchPayload resolveItem(FishingContext context) {
        Optional<FishingTableDefinition> tableOpt = findTable(context);
        if (tableOpt.isEmpty()) return DEFAULT_CATCH;
        FishingTableDefinition table = tableOpt.get();

        List<FishingTableDefinition.LootEntry> pool = table.items();
        double treasureChance = getTotalStatistic(context, ItemStatistic.TREASURE_CHANCE);
        if (context.bait() != null) {
            treasureChance += context.bait().getTreasureChanceBonus();
        }
        if (context.sinker() != null && context.sinker().isBayouTreasureToJunk()) {
            treasureChance += 10.0D;
        }

        if (!table.treasures().isEmpty() && rollChance("treasure", treasureChance)) {
            pool = context.sinker() != null && context.sinker().isBayouTreasureToJunk() ? table.junk() : table.treasures();
            return pick(pool, true).orElse(DEFAULT_CATCH);
        }

        return pick(pool, false)
            .or(() -> table.junk().isEmpty() ? Optional.empty() : pick(table.junk(), false))
            .orElse(DEFAULT_CATCH);
    }

    private static Optional<CatchPayload> pick(List<FishingTableDefinition.LootEntry> pool, boolean fromTreasure) {
        if (pool.isEmpty()) return Optional.empty();
        List<LootEntry<Void, FishingTableDefinition.LootEntry>> entries = pool.stream()
                .map(entry -> new LootEntry<Void, FishingTableDefinition.LootEntry>(key("item/" + entry.itemId()), entry, entry.chance()))
                .toList();
        return new LootTable<Void, FishingTableDefinition.LootEntry>(key("items"), List.of(
                new LootPool<>(key("items"), LootPool.Mode.WEIGHTED, entries)
        )).roll(null).stream().map(LootRoll::value).findFirst()
                .map(entry -> new CatchPayload.Item(entry.itemId(), entry.amount(), entry.skillXp(), fromTreasure));
    }

    private static Optional<FishingTableDefinition> findTable(FishingContext context) {
        FishingTableDefinition fallback = null;
        for (FishingTableDefinition definition : FishingRegistry.getTables()) {
            if (!definition.mediums().isEmpty() && !definition.mediums().contains(context.medium())) {
                continue;
            }
            if (context.regionId() != null && definition.regions().contains(context.regionId())) {
                return Optional.of(definition);
            }
            if (definition.regions().isEmpty() && fallback == null) {
                fallback = definition;
            }
        }
        return Optional.ofNullable(fallback);
    }

    private static boolean rollDoubleHook(FishingContext context) {
        double chance = getTotalStatistic(context, ItemStatistic.DOUBLE_HOOK_CHANCE);
        if (context.bait() != null) {
            chance += context.bait().getDoubleHookChanceBonus();
        }
        return chance > 0 && rollChance("double_hook", chance);
    }

    private static boolean rollChance(String id, double percent) {
        return LootTable.rollSingle(key(id), Boolean.TRUE,
                Math.clamp(percent / 100D, 0, 1)).isPresent();
    }

    private static Key key(String value) {
        return Key.key("skyblock", "fishing/" + value.toLowerCase());
    }

    private static double getTotalStatistic(FishingContext context, ItemStatistic statistic) {
        double total = context.rod().getAttributeHandler().getStatistics().getOverall(statistic)
            + FishingRodPartService.getStatistics(context.rod()).getOverall(statistic);
        total += context.hotspotBuffs().getOverall(statistic);
        if (context.bait() != null) {
            SkyBlockItem baitItem = FishingItemSupport.getItem(context.bait().getItemId());
            if (baitItem != null) {
                total += baitItem.getAttributeHandler().getStatistics().getOverall(statistic);
            }
        }
        return total;
    }

    private static double getTagBonus(FishingContext context, List<FishingTag> tags) {
        double total = 0.0D;
        if (context.hook() != null) {
            total += getTagBonus(context.hook().getTagBonuses(), tags);
        }
        if (context.line() != null) {
            total += getTagBonus(context.line().getTagBonuses(), tags);
        }
        if (context.sinker() != null) {
            total += getTagBonus(context.sinker().getTagBonuses(), tags);
        }
        if (context.bait() != null) {
            total += getTagBonus(context.bait().getTagBonuses(), tags);
        }
        return total;
    }

    private static double getTagBonus(java.util.Map<String, Double> bonuses, List<FishingTag> tags) {
        double total = 0.0D;
        for (FishingTag tag : tags) {
            total += bonuses.getOrDefault(tag.id(), 0.0D);
        }
        return total;
    }
}
