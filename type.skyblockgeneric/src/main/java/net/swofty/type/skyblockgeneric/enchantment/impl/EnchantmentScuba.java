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

public final class EnchantmentScuba implements Ench {
    @Override
    public String getDescription(int level) {
        return "Grants <a>+" + level + "<stat:pressure_resistance><7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 10,
                2, 20,
                3, 30,
                4, 40,
                5, 50,
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
                .withBase(ItemStatistic.PRESSURE_RESISTANCE, (double) level)
                .build();
    }
}
