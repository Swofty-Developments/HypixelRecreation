package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spinosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPINOSAURUS, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a Sea Creature spawn hook")
public final class SharpAttitudeAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Sharp Attitude";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<b>Sea Creatures <7>spawn with <a>" + decimalify(value, 1) + "% <7>of",
                "<7>their maximum health missing."
        );
    }
}
