package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.horse;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.HORSE, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a permanent potion-effect application system")
public final class HighStrideAbility implements PetAbility {

    @Override
    public String getName() {
        return "High Stride";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        int bonus = switch (rarity) {
            case COMMON -> 2;
            case UNCOMMON, RARE -> 3;
            case EPIC, LEGENDARY -> 4;
            default -> 2;
        };

        return List.of(
                "<7>Grants permanent <b>Jump Boost " + StringUtility.getAsRomanNumeral(bonus) + "<7>."
        );
    }
}
