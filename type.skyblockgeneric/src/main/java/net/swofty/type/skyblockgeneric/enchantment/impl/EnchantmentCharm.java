package net.swofty.type.skyblockgeneric.enchantment.impl;

import lombok.NonNull;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentCharm implements Ench {
    @Override
    public String getDescription(int level) {
        return "<7>Increases the chance to receive higher-tiered Trophy Fish by <a>" + level * 2 + "%</a>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NonNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.ofEntries(
            Map.entry(1, 10),
            Map.entry(2, 20),
            Map.entry(3, 30),
            Map.entry(4, 40),
            Map.entry(5, 50),
            Map.entry(6, 100)
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD);
    }
}
