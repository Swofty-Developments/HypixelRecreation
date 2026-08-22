package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.armadillo;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ARMADILLO, minimumRarity = Rarity.MYTHIC, order = 0,
        implemented = false, notImplementedReason = "tunneling energy system not implemented")
public final class WellWorkedAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Well-Worked";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Consumes <e>" + decimalify(value, 1) + "% <7>less energy when",
                "<7>tunneling."
        );
    }
}
