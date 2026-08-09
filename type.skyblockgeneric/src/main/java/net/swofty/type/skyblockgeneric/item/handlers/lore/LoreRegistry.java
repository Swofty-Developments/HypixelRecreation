package net.swofty.type.skyblockgeneric.item.handlers.lore;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.PotatoType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodLoreBuilder;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoreRegistry {
    private static final Map<String, LoreConfig> REGISTERED_HANDLERS = new HashMap<>();

    static {
        register("ENCHANTED_BOOK", new LoreConfig(
                (item, player) -> {
                    List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments().toList();
                    ArrayList<String> lore = new ArrayList<>();

                    enchantments.forEach(enchantment -> {
                        lore.add("§9" + StringUtility.toNormalCase(enchantment.type().name()) + " " +
                                StringUtility.getAsRomanNumeral(enchantment.level()));
                        StringUtility.splitByWordAndLength(enchantment.type().getDescription(enchantment.level(), player), 30)
                                .forEach(line -> lore.add("§7" + line));
                    });

                    lore.add(" ");
                    lore.add("§7Apply Cost: §3" + enchantments.stream()
                            .mapToInt(enchant -> enchant.type().getApplyCost(enchant.level(), player))
                            .sum() + " Exp Levels");
                    lore.add(" ");

                    Set<String> sourceTypes = enchantments.stream()
                            .flatMap(enchantment -> enchantment.type().getEnch().getGroups().stream())
                            .map(EnchantItemGroups::getDisplayName)
                            .collect(Collectors.toSet());

                    lore.add("§7Applicable on: §9" + String.join("§7, §9", sourceTypes));
                    lore.add("§7Use this on an item in an Anvil to");
                    lore.add("§7apply it!");

                    return lore;
                }, null));
        register("SKYBLOCK_MENU_LORE", new LoreConfig((item, player) -> Arrays.asList(
                "§7View all of your SkyBlock progress,",
                "§7including your Skills, Collections,",
                "§7Recipes, and more!",
                "§e ",
                "§eClick to open!"
        ), (item, player) -> "§aSkyBlock Menu §7(Click)"));
        register("HOT_POTATO_BOOK", new LoreConfig((item, player) -> PotatoType.allLores(), null));
        register("FISHING_ROD", new LoreConfig(
            (item, player) -> {
                var lore = FishingRodLoreBuilder.build(item, player);
                return lore == null ? List.of() : lore.lore();
            },
            (item, player) -> {
                var lore = FishingRodLoreBuilder.build(item, player);
                return lore == null ? item.getDisplayName() : lore.displayName();
            }
        ));
        register("MIDAS_SWORD", new LoreConfig((item, player) -> {
            List<String> lore = new ArrayList<>();
            long pricePaid = item.getAttributeHandler().getDarkAuctionPrice();
            int greedBonus = calculateGreedBonus(pricePaid);

            lore.add("§7Price paid: §6" + StringUtility.commaify(pricePaid) + " Coins");
            lore.add("§c❁ Strength §7bonus: §c+" + greedBonus);
            lore.add("§c❁ Damage §7bonus: §c+" + greedBonus);

            return lore;
        }, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    /**
     * Calculates the Greed bonus for Midas' Sword based on the price paid.
     * Tiered formula from the wiki.
     */
    private static int calculateGreedBonus(long price) {
        if (price >= 50_000_000L) return 120;
        if (price >= 25_000_000L) {
            int bonus = 95 + (int)((price - 25_000_000L) / 1_000_000L);
            return Math.min(120, bonus);
        }
        if (price >= 7_500_000L) {
            int bonus = 60 + (int)((price - 7_500_000L) / 500_000L);
            return Math.min(95, bonus);
        }
        if (price >= 2_500_000L) {
            int bonus = 35 + (int)((price - 2_500_000L) / 200_000L);
            return Math.min(60, bonus);
        }
        if (price >= 1_000_000L) {
            int bonus = 20 + (int)((price - 1_000_000L) / 100_000L);
            return Math.min(35, bonus);
        }
        return (int) Math.min(20, price / 50_000L);
    }

    public static void register(String id, LoreConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static LoreConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}
