package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentGiantKiller implements Ench, EnchFromTable, EventBasedEnchant {

    public static final double[] DAMAGE_MULTIPLIERS = new double[]{0.1, 0.2, 0.3, 0.4, 0.6, 0.9, 1.2};
    public static final double[] DAMAGE_CAPS = new double[]{5.0, 10.0, 15.0, 20.0, 30.0, 45.0, 65.0};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt by <a>" + DAMAGE_MULTIPLIERS[level - 1]
                + "%<7> for each percent of extra health that your target has above you up to <a>"
                + DAMAGE_CAPS[level - 1] + "%<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                4, 36,
                5, 45,
                6, 100,
                7, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.GIANT_KILLER_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null || receiver == null) {
            return ItemStatistics.empty();
        }

        double playerMaxHealth = causer.getStatistics().allStatistics().getOverall(ItemStatistic.HEALTH);
        double targetMaxHealth = receiver.getAttributeValue(Attribute.MAX_HEALTH);

        if (targetMaxHealth <= playerMaxHealth) {
            return ItemStatistics.empty();
        }

        double healthPercentageDifference = ((targetMaxHealth - playerMaxHealth) / playerMaxHealth) * 100;
        double damageMultiplier = Math.min(healthPercentageDifference * DAMAGE_MULTIPLIERS[level - 1], DAMAGE_CAPS[level - 1]);

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damageMultiplier).build();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 20,
                3, 30,
                4, 40,
                5, 50
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.GIANT_KILLER_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 4;
    }
}