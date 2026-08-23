package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentVicious implements Ench {

    public static final int[] FEROCITY_BONUSES = new int[]{3, 4, 5};

    @Override
    public String getDescription(int level) {
        int ferocityBonus = FEROCITY_BONUSES[level - 3];
        return "Grants <a>+" + ferocityBonus + " ⫽Ferocity<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                3, 60,
                4, 80,
                5, 100
        ));

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        int ferocityBonus = FEROCITY_BONUSES[level - 3];
        return ItemStatistics.builder().withBase(ItemStatistic.FEROCITY, (double) ferocityBonus).build();
    }
}
