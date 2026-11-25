package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tyrannosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TYRANNOSAURUS, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a pet-item system")
public final class TyrantAbility implements PetAbility {
    private static final double PER_LEVEL = 1.0;

    @Override
    public String getName() {
        return "Tyrant";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Combat stats granted by pet items on",
                "<7>this pet are increased by <a>" + decimalify(value, 1) + "%<7>."
        );
    }
}
