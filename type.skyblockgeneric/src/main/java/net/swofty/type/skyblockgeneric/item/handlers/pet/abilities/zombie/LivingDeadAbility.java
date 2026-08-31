package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.zombie;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ZOMBIE, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits Undead armor items + an armor-stats-increase hook")
public final class LivingDeadAbility implements PetAbility {
    private static final double PER_LEVEL = 0.25;

    @Override
    public String getName() {
        return "Living Dead";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increases all stats on",
                "<2>Undead <7>armor by <a>" + decimalify(value, 1) + "%<7>."
        );
    }
}
