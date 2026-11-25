package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentSmite implements Ench, EnchFromTable, EventBasedEnchant {

    public static final double[] MULTIPLIERS = new double[]{5, 10, 15, 20, 30, 40, 50};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to " + MobType.WITHER.getFullDisplayName().serialize()
                + ", " + MobType.SKELETAL.getFullDisplayName().serialize()
                + " and " + MobType.UNDEAD.getFullDisplayName().serialize()
                + "<7> mobs by " + MULTIPLIERS[level - 1] + "%.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 13,
                3, 18,
                4, 23,
                5, 27,
                6, 91,
                7, 179
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.SMITE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (receiver instanceof SkyBlockMob skyBlockMob) {
            if (skyBlockMob.getMobTypes().contains(MobType.WITHER) ||
                    skyBlockMob.getMobTypes().contains(MobType.SKELETAL) ||
                    skyBlockMob.getMobTypes().contains(MobType.UNDEAD)) {
                return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, MULTIPLIERS[level - 1]).build();
            }
        }

        return ItemStatistics.empty();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 15,
                3, 20,
                4, 25,
                5, 30
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.SMITE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
