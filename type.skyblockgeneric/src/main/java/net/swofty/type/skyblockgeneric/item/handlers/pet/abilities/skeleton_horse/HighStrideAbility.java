package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.skeleton_horse;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.SKELETON_HORSE, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a permanent potion-effect application system")
public final class HighStrideAbility implements PetAbility {

    @Override
    public String getName() {
        return "High Stride";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants permanent <b>Jump Boost " + StringUtility.getAsRomanNumeral(4) + "<7>."
        );
    }
}
