package net.swofty.type.skyblockgeneric.enchantment.impl;

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

public class EnchantmentManaSteal implements Ench, EnchFromTable, DamageEventEnchant, ConflictingEnch {

    public static final double[] MANA_REGAIN_PERCENTAGES = new double[]{0.0025, 0.005, 0.0075};

    @Override
    public String getDescription(int level) {
        double manaPercent = MANA_REGAIN_PERCENTAGES[level - 1] * 100;
        return "Regain <a>" + manaPercent + "%<7> of your mana on hit.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30
        ));

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>());
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, net.minestom.server.entity.LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob)) return;

        double manaPercent = MANA_REGAIN_PERCENTAGES[level - 1];
        float manaRegain = (float) (player.getMaxMana() * manaPercent);

        float newMana = Math.min(player.getMana() + manaRegain, player.getMaxMana());
        player.setMana(newMana);
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.LIFE_STEAL, EnchantmentType.DRAIN);
    }
}


