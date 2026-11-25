package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blaze;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.BLAZE, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Hot Potato Book system")
public final class FusionStylePotatoAbility implements PetAbility {
    @Override
    public String getName() {
        return "Fusion-Style Potato";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Double effects of hot potato",
                "<7>books."
        );
    }
}
