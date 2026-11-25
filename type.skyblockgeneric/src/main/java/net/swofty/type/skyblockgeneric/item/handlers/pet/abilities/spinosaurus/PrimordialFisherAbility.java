package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spinosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPINOSAURUS, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a weather/rain system")
public final class PrimordialFisherAbility implements PetAbility {
    private static final double PER_LEVEL = 0.5;

    @Override
    public String getName() {
        return "Primordial Fisher";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increases this pet's base stats by",
                "<a>" + decimalify(value, 1) + "% <7>during <b>rain<7>."
        );
    }
}
