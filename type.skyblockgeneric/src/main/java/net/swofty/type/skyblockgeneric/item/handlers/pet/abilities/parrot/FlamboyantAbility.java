package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.parrot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PARROT, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits an intimidation accessories system")
public final class FlamboyantAbility implements PetAbility {
    @Override
    public String getName() {
        return "Flamboyant";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Adds <a>1 <7>level(s) to",
                "<7>intimidation accessories."
        );
    }
}
