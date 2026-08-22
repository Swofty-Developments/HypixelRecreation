package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rock;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.ROCK, minimumRarity = Rarity.COMMON, order = 0,
        implemented = false, notImplementedReason = "pet summon must be a rideable mount type")
public final class RideableAbility implements PetAbility {
    @Override
    public String getName() {
        return "Rideable";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Right-click your summoned pet to",
                "<7>ride it!"
        );
    }
}
