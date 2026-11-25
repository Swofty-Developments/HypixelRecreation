package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.RAT, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits a Cheese finding system (CheeseFound event)")
public final class CHEESEAbility implements PetAbility {
    @Override
    public String getName() {
        return "CHEESE!";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>As a Rat, you smell <e><l>CHEESE<r>",
                "<7>nearby! Yummy!"
        );
    }
}
