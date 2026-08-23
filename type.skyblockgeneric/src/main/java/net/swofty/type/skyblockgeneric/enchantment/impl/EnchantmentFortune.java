package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentFortune implements Ench, EnchFromTable, ConflictingEnch {

    public static final int[] MINING_FORTUNE_BONUS = new int[]{10, 20, 30, 45};

    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + MINING_FORTUNE_BONUS[level - 1] + " " + ItemStatistic.MINING_FORTUNE.getDisplayName()
                + "<7>, which increases your chance for multiple drops.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                4, 0
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FORTUNE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.GAUNTLET, EnchantItemGroups.PICKAXE, EnchantItemGroups.DRILL);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.MINING_FORTUNE, (double) MINING_FORTUNE_BONUS[level - 1])
                .build();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 45
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FORTUNE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.SILK_TOUCH);
    }
}