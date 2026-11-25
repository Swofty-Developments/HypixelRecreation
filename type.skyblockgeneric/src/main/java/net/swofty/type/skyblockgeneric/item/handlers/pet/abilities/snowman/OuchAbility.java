package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snowman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.SNOWMAN, minimumRarity = Rarity.MYTHIC, order = 3,
        implemented = false, notImplementedReason = "awaits a snowball projectile system")
public final class OuchAbility implements PetAbility {

    @Override
    public String getName() {
        return "Ouch!";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Your snowballs have <a>50% <7>chance of",
                "<7>dealing <c>double <7>damage!"
        );
    }
}
