package net.swofty.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentLooting implements Ench, EnchFromTable {
    public static final int[] MULTIPLIERS = new int[]{15, 30, 45, 60, 75};

    @Override
    public String getDescription(int level) {
        return "Increases the chance of a Monster dropping an item by <a>" + MULTIPLIERS[level - 1] + "%<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 45,
                4, 100,
                5, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.LOOTING_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(
                EnchantItemGroups.SWORD,
                EnchantItemGroups.LONG_SWORD,
                EnchantItemGroups.GAUNTLET,
                EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.FISHING_ROD
        );
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 45
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.LOOTING_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 3;
    }
}
