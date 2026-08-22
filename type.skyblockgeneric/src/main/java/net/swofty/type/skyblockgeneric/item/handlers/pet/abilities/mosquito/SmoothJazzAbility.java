package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mosquito;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOSQUITO, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Pest Vinyl system")
public final class SmoothJazzAbility implements PetAbility {
    private static final RarityValue<Double> EFFECTIVENESS_PER_LEVEL =
            new RarityValue<>(0.25, 0.25, 0.35, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Smooth Jazz";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(EFFECTIVENESS_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Pest Vinyls are <a>+" + percent + "% <7>more effective."
        );
    }
}
