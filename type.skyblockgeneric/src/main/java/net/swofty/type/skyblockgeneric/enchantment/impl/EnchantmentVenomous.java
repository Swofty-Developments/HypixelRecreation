package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.debuff.VenomousDebuff;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentVenomous implements Ench, EnchFromTable, DamageEventEnchant {

    @Override
    public String getDescription(int level) {
        double speedReduction = VenomousDebuff.SPEED_REDUCTION_PERCENTAGES[level - 1] * 100;
        double damagePercent = level * 0.3;
        return "Reduces the target's walk speed by <a>" + speedReduction + "%<7> and deals <a>+" + damagePercent + "%<7> of your damage per second per hit, stacking globally up to 40 hits. Lasts 5s.";
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

        if (player.hasCustomCollectionAward(CustomCollectionAward.VENOMOUS_DISCOUNT)) {
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
        return 20;
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
    public void onDamageDealt(SkyBlockPlayer player, net.minestom.server.entity.LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > 6) return;
        if (!(target instanceof SkyBlockMob mob)) return;

        VenomousDebuff.applyVenomous(player, mob, damageDealt, level);
    }
}
