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

public final class EnchantmentPesterminator implements Ench {
    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + level * 2 + "<stat:farming_fortune> <7>and <a>+" + level
                + "<stat:bonus_pest_chance><7>, which increases your chance to spawn bonus <2>Pests <7>on The Garden.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 5,
                2, 9,
                3, 13,
                4, 18,
                5, 23,
                6, 0
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, (double) level * 2)
                .withBase(ItemStatistic.BONUS_PEST_CHANCE, (double) level)
                .build();
    }
}
