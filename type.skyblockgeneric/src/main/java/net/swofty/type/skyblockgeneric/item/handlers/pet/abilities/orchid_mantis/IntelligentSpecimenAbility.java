package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.orchid_mantis;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ORCHID_MANTIS, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Farming Tool EXP system")
public final class IntelligentSpecimenAbility implements PetAbility {
    private static final RarityValue<Double> EXP_PER_LEVEL =
            new RarityValue<>(0.1, 0.2, 0.2, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Intelligent Specimen";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(EXP_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Earn <a>+" + percent + "% <7>more Farming Tool Exp."
        );
    }
}
