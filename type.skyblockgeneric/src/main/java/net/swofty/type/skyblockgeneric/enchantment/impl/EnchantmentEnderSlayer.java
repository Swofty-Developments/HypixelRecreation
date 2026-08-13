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

public class EnchantmentEnderSlayer implements Ench, EnchFromTable, EventBasedEnchant {

    public static final double[] MULTIPLIERS = new double[]{5, 10, 15, 20, 30, 40, 50};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to " + MobType.ENDER.getFullDisplayName().serialize()
                + "<7> mobs by <a>" + MULTIPLIERS[level - 1] + "%<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                4, 27,
                5, 36,
                6, 58,
                7, 179
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.ENDER_SLAYER_DISCOUNT)) {
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
        if (receiver instanceof SkyBlockMob skyBlockMob && skyBlockMob.getMobTypes().contains(MobType.ENDER)) {
            return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, MULTIPLIERS[level - 1]).build();
        }

        return ItemStatistics.empty();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 20,
                3, 25,
                4, 30,
                5, 40
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.ENDER_SLAYER_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
