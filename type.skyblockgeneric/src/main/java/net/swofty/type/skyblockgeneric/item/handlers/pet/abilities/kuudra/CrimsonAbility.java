package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.kuudra;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.KUUDRA, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a Crimson Essence drop system")
public final class CrimsonAbility implements PetAbility {
    private static final RarityValue<Double> ESSENCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.15, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Crimson";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(ESSENCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <a>" + percent + "% <7>extra Crimson",
                "<7>Essence."
        );
    }
}
