package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.witch;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WITCH, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits the Year of the Witch event")
public final class WitchingHourAbility implements PetAbility {
    private static final double PER_LEVEL = 1.0;

    @Override
    public String getName() {
        return "Witching Hour";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increases the base stats of this pet",
                "<7>by <a>" + decimalify(value, 1) + "% <7>during the <5>Year of the Witch<7>."
        );
    }
}
