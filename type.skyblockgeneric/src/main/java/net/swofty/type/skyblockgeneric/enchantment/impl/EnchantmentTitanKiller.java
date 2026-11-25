package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

public final class EnchantmentTitanKiller extends ExperimentRewardEnchantment {
    public EnchantmentTitanKiller() {
        super("Increases damage dealt based on the target's Defense.", EnchantItemGroups.SWORD,
                EnchantItemGroups.LONG_SWORD, EnchantItemGroups.BOW);
    }
}
