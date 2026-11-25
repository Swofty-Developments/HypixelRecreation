package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mole;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOLE, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits an Automaton mob drop system")
public final class MagneticNoseAbility implements PetAbility {
    private static final RarityValue<Double> DROP_RATE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Magnetic Nose";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DROP_RATE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<9>Automatons <7>drop their parts <a>" + percent + "%",
                "<7>more frequently."
        );
    }
}
