package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.megalodon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.MEGALODON, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a Fishing Festival system (calendar event)")
public final class EnhancedScalesAbility implements PetAbility {
    @Override
    public String getName() {
        return "Enhanced Scales";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Doubles the pet's base stats during",
                "<7>the <b>Fishing Festival<7>."
        );
    }
}
