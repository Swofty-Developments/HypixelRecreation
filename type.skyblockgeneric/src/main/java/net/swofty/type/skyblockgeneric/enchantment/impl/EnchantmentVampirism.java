package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentVampirism implements Ench, EnchFromTable, KillEventEnchant {

    public static final double[] HEAL_PERCENTAGES = new double[]{0.01, 0.02, 0.03, 0.04, 0.05, 0.06};

    @Override
    public String getDescription(int level) {
        double healPercent = HEAL_PERCENTAGES[level - 1] * 100;
        return "Heals for <a>" + healPercent + "%<7> of your missing health whenever you kill an enemy.";
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

        if (player.hasCustomCollectionAward(CustomCollectionAward.VAMPIRISM_DISCOUNT)) {
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
    public int getRequiredBookshelfPower() {
        return 15;
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 1,
                2, 2,
                3, 3,
                4, 4,
                5, 5
        ));

        return new TableLevels(levels);
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        if (level < 1 || level > 6) return;

        double healPercent = HEAL_PERCENTAGES[level - 1];
        float missingHealth = player.getMaxHealth() - player.getHealth();
        float healAmount = (float) (missingHealth * healPercent);
        float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);
    }
}
