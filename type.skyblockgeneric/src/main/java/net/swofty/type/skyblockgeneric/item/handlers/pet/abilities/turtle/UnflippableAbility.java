package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.turtle;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.TURTLE, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a knockback-application hook")
public final class UnflippableAbility implements PetAbility {

    @Override
    public String getName() {
        return "Unflippable";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Gain <a>immunity <7>to knockback."
        );
    }
}
