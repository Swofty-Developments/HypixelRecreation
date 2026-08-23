package net.swofty.type.skyblockgeneric.fishing.rod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DefaultSoulboundComponent;
import net.swofty.type.skyblockgeneric.item.components.FishingRodMetadataComponent;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingRodLoreBuilder {
    private static final List<ItemStatistic> STAT_ORDER = List.of(
        ItemStatistic.DAMAGE,
        ItemStatistic.STRENGTH,
        ItemStatistic.FEROCITY,
        ItemStatistic.FISHING_SPEED,
        ItemStatistic.SEA_CREATURE_CHANCE,
        ItemStatistic.DOUBLE_HOOK_CHANCE,
        ItemStatistic.TROPHY_FISH_CHANCE
    );

    public static @Nullable FishingRodLore build(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        if (!item.hasComponent(FishingRodMetadataComponent.class)) {
            return null;
        }

        FishingRodMetadataComponent metadata = item.getComponent(FishingRodMetadataComponent.class);
        List<String> lore = new ArrayList<>();
        if (metadata.getSubtitle() != null && !metadata.getSubtitle().isBlank()) {
            lore.add(metadata.getSubtitle());
        }

        for (ItemStatistic statistic : STAT_ORDER) {
            double amount = item.getAttributeHandler().getStatistics().getOverall(statistic)
                + FishingRodPartService.getStatistics(item).getOverall(statistic);
            if (amount == 0.0D) {
                continue;
            }
            lore.add(formatStatistic(statistic, amount));
        }

        if (item.hasComponent(GemstoneComponent.class)) {
            lore.add(renderGemSlots(item.getComponent(GemstoneComponent.class)));
        }

        lore.add(partLine("ථ Hook ", item.getAttributeHandler().getFishingHook()));
        lore.add(partLine("ꨃ Line ", item.getAttributeHandler().getFishingLine()));
        lore.add(partLine("࿉ Sinker ", item.getAttributeHandler().getFishingSinker()));

        if (metadata.getLegacyConversionTarget() != null) {
            lore.add("Talk to Roddy in the Backwater");
            lore.add("Bayou to convert this rod.");
        } else if (metadata.isRodPartsEnabled()) {
            lore.add("Talk to Roddy in the Backwater");
            lore.add("Bayou to apply parts to this rod.");
        }

        if (item.hasComponent(net.swofty.type.skyblockgeneric.item.components.ReforgableComponent.class)) {
            lore.add("This item can be reforged!");
        }
        if (metadata.getRequiredFishingLevel() > 0) {
            lore.add("❣ Requires Fishing Skill " + metadata.getRequiredFishingLevel() + ".");
        }
        if (metadata.getExtraRequirements() != null) {
            lore.addAll(metadata.getExtraRequirements());
        }
        if (item.hasComponent(DefaultSoulboundComponent.class)) {
            lore.add("* Co-op Soulbound *");
        }

        lore.add(rarityLine(item.getAttributeHandler().getRarity()));
        return new FishingRodLore(item.getDisplayName(), lore);
    }

    private static String rarityLine(Rarity rarity) {
        return Text.of("<color:{0}><l>{1} FISHING ROD",
            rarity.getColor(), rarity.name().replace("_", " ")).serialize();
    }

    private static String renderGemSlots(GemstoneComponent gemstone) {
        return gemstone.getSlots().stream()
            .map(slot -> "[" + slot.slot().getSymbol() + "]")
            .reduce((left, right) -> left + " " + right)
            .orElse("");
    }

    private static String partLine(String prefix, @Nullable String itemId) {
        ItemType type = itemId == null ? null : ItemType.get(itemId);
        return prefix + (type == null ? "NONE" : type.getDisplayName());
    }

    private static String formatStatistic(ItemStatistic statistic, double amount) {
        return statistic.getDisplayName() + ": " + statistic.getPrefix() + format(amount) + statistic.getSuffix();
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.valueOf((long) Math.rint(value));
        }
        return StringUtility.decimalify(value, 1);
    }

    public record FishingRodLore(String displayName, List<String> lore) {
    }
}
