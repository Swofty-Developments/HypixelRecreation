package net.swofty.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentExpertise implements Ench {
    private static final double[] SEA_CREATURE_CHANCE_BONUS = {
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
    };

    @Override
    public String getDescription(int level) {
        return "Tracks <3>Sea Creature <7>kills on this rod. Every kill grants <b>+0.1☂ Fishing Wisdom<7>. "
            + "Current tier grants <3>+" + SEA_CREATURE_CHANCE_BONUS[level - 1] + "% Sea Creature Chance<7>.";
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder()
            .withBase(ItemStatistic.SEA_CREATURE_CHANCE, SEA_CREATURE_CHANCE_BONUS[level - 1])
            .build();
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
            1, 50,
            2, 100,
            3, 200,
            4, 400,
            5, 800,
            6, 1600,
            7, 3200,
            8, 6400,
            9, 12800,
            10, 25600
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD);
    }
}
