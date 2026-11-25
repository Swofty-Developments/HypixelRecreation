package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

abstract class ExperimentRewardEnchantment implements Ench {
    private final String description;
    private final List<EnchantItemGroups> groups;

    protected ExperimentRewardEnchantment(String description, EnchantItemGroups... groups) {
        this.description = description;
        this.groups = List.of(groups);
    }

    @Override
    public String getDescription(int level) {
        return description.replace("{level}", String.valueOf(level));
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return groups;
    }
}
