package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentProsecute implements Ench, EnchFromTable, EventBasedEnchant, ConflictingEnch {

    public static final double[] DAMAGE_BONUSES = new double[]{0.001, 0.002, 0.003, 0.004, 0.007, 0.01};

    @Override
    public String getDescription(int level) {
        double damagePercent = DAMAGE_BONUSES[level - 1] * 100;
        return "Increases damage dealt by <a>" + damagePercent + "%<7> for each percent of health your target has.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30,
                4, 40,
                5, 50,
                6, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.PROSECUTE_DISCOUNT)) {
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
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30,
                4, 40,
                5, 50
        ));

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 12;
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null || receiver == null) {
            return ItemStatistics.empty();
        }

        double targetMaxHealth = receiver.getAttributeValue(Attribute.MAX_HEALTH);
        double targetCurrentHealth = receiver.getHealth();

        if (targetMaxHealth <= 0) {
            return ItemStatistics.empty();
        }

        double healthPercentage = targetCurrentHealth / targetMaxHealth;

        double damageBonusPerPercent = DAMAGE_BONUSES[level - 1];
        double totalDamageBonus = healthPercentage * damageBonusPerPercent;

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, totalDamageBonus).build();
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.EXECUTE);
    }
}

