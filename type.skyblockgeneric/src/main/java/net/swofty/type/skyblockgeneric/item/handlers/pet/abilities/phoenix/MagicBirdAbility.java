package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.phoenix;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PHOENIX, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a private-island flight permission system")
public final class MagicBirdAbility implements PetAbility {
    @Override
    public String getName() {
        return "Magic Bird";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>You may always <e>fly <7>on your",
                "<7>private island."
        );
    }
}
