package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentDrain implements Ench, EnchFromTable, DamageEventEnchant, ConflictingEnch {

    public static final double[] HEAL_PERCENTAGES = new double[]{0.002, 0.003, 0.004, 0.005, 0.006};

    private static final double MAX_CRIT_DAMAGE = 1000.0;

    @Override
    public String getDescription(int level) {
        double healPercent = HEAL_PERCENTAGES[level - 1] * 100;
        return "Heals for <a>" + healPercent + "%<7> of your max health per 100 ☠Crit Damage you deal per hit, up to 1,000 ☠Crit Damage.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30,
                4, 50,
                5, 200
        ));

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30
        ));

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 5;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob)) return;

        double critDamageDealt = calculateCritDamageDealt(player, damageDealt);
        if (critDamageDealt <= 0) return;

        critDamageDealt = Math.min(critDamageDealt, MAX_CRIT_DAMAGE);

        double healPercent = HEAL_PERCENTAGES[level - 1];
        double healMultiplier = critDamageDealt / 100.0;
        float healAmount = (float) (healMultiplier * healPercent * player.getMaxHealth());

        float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);
    }

    private double calculateCritDamageDealt(SkyBlockPlayer player, double totalDamageDealt) {
        double critDamageStat = player.getStatistics().allStatistics().getOverall(ItemStatistic.CRITICAL_DAMAGE);

        double baseDamage = totalDamageDealt / (1 + (critDamageStat / 100));

        return totalDamageDealt - baseDamage;
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.LIFE_STEAL, EnchantmentType.MANA_STEAL);
    }
}


