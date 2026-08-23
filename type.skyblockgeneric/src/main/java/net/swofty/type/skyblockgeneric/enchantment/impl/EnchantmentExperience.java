package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentExperience implements Ench, EnchFromTable {

    public static final double[] CHANCE_PERCENTAGES = new double[]{0.125, 0.25, 0.375, 0.50, 0.625};

    @Override
    public String getDescription(int level) {
        double chance = CHANCE_PERCENTAGES[level - 1] * 100;
        return "Grants a <a>" + chance + "%<7> chance for mobs and ores to drop double experience.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 45,
                4, 75,
                5, 0
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EXPERIENCE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD,
                EnchantItemGroups.PICKAXE, EnchantItemGroups.DRILL);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 45
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EXPERIENCE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    public static boolean shouldDoubleExperience(int level) {
        if (level < 1 || level > 5) return false;
        return Math.random() < CHANCE_PERCENTAGES[level - 1];
    }
}

