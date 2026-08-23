package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.FishingBaitComponent;
import net.swofty.type.skyblockgeneric.item.components.FishingRodPartComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingGuideStackFactory {
    private static final List<ItemStatistic> GUIDE_STAT_ORDER = List.of(
        ItemStatistic.DAMAGE,
        ItemStatistic.STRENGTH,
        ItemStatistic.FEROCITY,
        ItemStatistic.FISHING_SPEED,
        ItemStatistic.SEA_CREATURE_CHANCE,
        ItemStatistic.DOUBLE_HOOK_CHANCE,
        ItemStatistic.TREASURE_CHANCE,
        ItemStatistic.TROPHY_FISH_CHANCE,
        ItemStatistic.MAGIC_FIND
    );

    public static net.minestom.server.item.ItemStack.Builder buildBaitStack(SkyBlockItem baitItem) {
        FishingBaitComponent bait = baitItem.getComponent(FishingBaitComponent.class);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>Fishing Bait"));
        lore.add(Text.of("<8>Consumes on Cast"));
        lore.add(Text.empty());
        appendStatistics(lore, baitItem.getAttributeHandler().getStatistics());
        appendTagBonuses(lore, bait.getTagBonuses());

        if (bait.getTreasureChanceBonus() > 0) {
            lore.add(Text.of("<7>Grants <6>+{} Treasure Chance<7>.", format(bait.getTreasureChanceBonus())));
        }
        if (bait.getTreasureQualityBonus() > 0) {
            lore.add(Text.of("<7>Increases treasure quality by <a>{}%<7>.", format(bait.getTreasureQualityBonus())));
        }
        if (bait.getTrophyFishChanceBonus() > 0) {
            lore.add(Text.of("<7>Grants <6>+{} Trophy Fish Chance<7>.", format(bait.getTrophyFishChanceBonus())));
        }
        if (bait.getDoubleHookChanceBonus() > 0) {
            lore.add(Text.of("<7>Grants <9>+{} Double Hook Chance<7>.", format(bait.getDoubleHookChanceBonus())));
        }
        if (bait.getMediums().size() == 1) {
            lore.add(Text.of("<7>Usable in {}<7>.", bait.getMediums().getFirst() == FishingMedium.WATER
                ? Text.of("<b>Water")
                : Text.of("<c>Lava")));
        }
        finishFooter(lore, bait.getItemId(), "BAIT");

        return ItemStacks.head(bait.getTexture(), colouredName(bait.getItemId(), bait.getDisplayName()), lore);
    }

    public static net.minestom.server.item.ItemStack.Builder buildRodPartStack(SkyBlockItem partItem) {
        FishingRodPartComponent part = partItem.getComponent(FishingRodPartComponent.class);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{} Rod Part", StringUtility.toNormalCase(part.getCategory().name())));
        lore.add(Text.empty());
        appendStatistics(lore, partItem.getAttributeHandler().getStatistics());
        appendTagBonuses(lore, part.getTagBonuses());

        if (part.isTreasureOnly()) {
            lore.add(Text.of("<7>Only allows you to catch items and <6>Treasure<7>."));
        }
        if (part.isBayouTreasureToJunk()) {
            lore.add(Text.of("<7>Replaces <6>Treasure <7>catches with <2>Junk <7>in the <2>Backwater Bayou<7>."));
        }
        if (part.getMaterializedItemId() != null) {
            String itemName = ItemType.valueOf(part.getMaterializedItemId()).getDisplayName();
            if (part.getMaterializedChance() >= 1.0D) {
                lore.add(Text.of("<7>Materializes <f>{} <7>in your inventory whenever you catch something.", itemName));
            } else {
                lore.add(Text.of("<7>Has a <a>{}% <7>chance to materialize <f>{}<7>.",
                    format(part.getMaterializedChance() * 100.0D), itemName));
            }
        }
        if (part.getBaitPreservationChance() > 0) {
            lore.add(Text.of("<7>Grants a <a>{}% <7>chance to not consume Bait.", format(part.getBaitPreservationChance())));
        }
        if (part.getHotspotBuffMultiplier() > 1.0D) {
            lore.add(Text.of("<7>Increases the bonuses of <d>Fishing Hotspots <7>by <a>{}%<7>.",
                format((part.getHotspotBuffMultiplier() - 1.0D) * 100.0D)));
        }
        if (part.getRequiredFishingLevel() > 0) {
            lore.add(Text.empty());
            lore.add(Text.of("<4>❣ <c>Requires <a>Fishing Skill {}<c>.", part.getRequiredFishingLevel()));
        }
        finishFooter(lore, part.getItemId(), "ROD PART");

        return ItemStacks.head(part.getTexture(), colouredName(part.getItemId(), part.getDisplayName()), lore);
    }

    private static void appendStatistics(List<Text> lore, ItemStatistics statistics) {
        for (ItemStatistic statistic : GUIDE_STAT_ORDER) {
            double amount = statistics.getOverall(statistic);
            if (amount == 0) {
                continue;
            }
            lore.add(Text.of("<7>{}: <color:{}>{}{}{}", statistic.getDisplayName(),
                statistic.getLoreColor(), statistic.getPrefix(), format(amount), statistic.getSuffix()));
        }
    }

    private static void appendTagBonuses(List<Text> lore, Map<String, Double> bonuses) {
        for (Map.Entry<String, Double> entry : bonuses.entrySet()) {
            lore.add(Text.of("<7>Increases the chance to catch <e>{} <7>by <a>{}%<7>.",
                describeTag(entry.getKey()), format(entry.getValue())));
        }
    }

    private static void finishFooter(List<Text> lore, String itemId, String suffix) {
        if (!lore.isEmpty() && !lore.getLast().isEmpty()) {
            lore.add(Text.empty());
        }
        Rarity rarity = ItemType.valueOf(itemId).rarity;
        lore.add(Text.of("<color:{}><l>{} {}", rarity.getColor(), rarity.name().replace("_", " "), suffix));
    }

    private static Text colouredName(String itemId, String displayName) {
        return Text.of("<color:{}>{}", ItemType.valueOf(itemId).rarity.getColor(), displayName);
    }

    private static String describeTag(String tag) {
        return switch (tag.toUpperCase()) {
            case "COMMON" -> "Common Sea Creatures";
            case "HOTSPOT" -> "Hotspot Sea Creatures";
            case "SPOOKY" -> "Spooky Sea Creatures";
            case "WINTER" -> "Winter Sea Creatures";
            case "SHARK" -> "Sharks";
            default -> StringUtility.toNormalCase(tag.toLowerCase().replace('_', ' '));
        };
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.valueOf((long) Math.rint(value));
        }
        return StringUtility.decimalify(value, 1);
    }
}
