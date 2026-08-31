package net.swofty.type.skyblockgeneric.loottable;

import lombok.Getter;
import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentLooting;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentLuck;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

@Getter
public enum LootAffector {

    MAGIC_FIND((player, original, mob) -> {
        if (original >= 0.05) return original;
        if (mob != null) {
            return original + (original * player.getStatistics().allStatistics(player, mob).getOverall(ItemStatistic.MAGIC_FIND) * 0.01);
        } else {
            return original + (original * player.getStatistics().allStatistics().getOverall(ItemStatistic.MAGIC_FIND) * 0.01);
        }
    }),

    PET_LUCK((player, original, mob) -> {
        if (mob != null) {
            return original + (original * player.getStatistics().allStatistics(player, mob).getOverall(ItemStatistic.PET_LUCK) * 0.01);
        } else {
            return original + (original * player.getStatistics().allStatistics().getOverall(ItemStatistic.PET_LUCK) * 0.01);
        }
    }),

    ENCHANTMENT_LUCK((player, original, mob) -> {
        Map<Integer, Integer> enchants = player.getStatistics().getAllOfEnchants(EnchantmentType.LUCK, true);
        int total = 0;
        for (int level : enchants.keySet()) {
            total += EnchantmentLuck.MULTIPLIERS[level - 1] * enchants.get(level);
        }
        return original + (original * total * 0.01);
    }),

    ENCHANTMENT_LOOTING((player, original, mob) -> {
        Map<Integer, Integer> enchants = player.getStatistics().getAllOfEnchants(EnchantmentType.LOOTING, true);
        int total = 0;
        for (int level : enchants.keySet()) {
            total += EnchantmentLooting.MULTIPLIERS[level - 1] * enchants.get(level);
        }
        return original + (original * total * 0.01);
    });

    private final LootAffectorFunction affector;

    LootAffector(LootAffectorFunction affector) {
        this.affector = affector;
    }

    public double apply(SkyBlockPlayer player, double original) {
        return affector.apply(player, original, null);
    }

    public double apply(SkyBlockPlayer player, double original, LivingEntity mob) {
        return affector.apply(player, original, mob);
    }

    @FunctionalInterface
    public interface LootAffectorFunction {
        double apply(SkyBlockPlayer player, double original, LivingEntity mob);
    }
}
