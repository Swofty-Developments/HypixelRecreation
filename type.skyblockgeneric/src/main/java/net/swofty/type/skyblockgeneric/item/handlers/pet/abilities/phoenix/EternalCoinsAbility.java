package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.phoenix;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PHOENIX, minimumRarity = Rarity.LEGENDARY, order = 3,
        implemented = false, notImplementedReason = "awaits a death coin-loss system")
public final class EternalCoinsAbility implements PetAbility {
    @Override
    public String getName() {
        return "Eternal Coins";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Don't lose <6>coins <7>from death."
        );
    }
}
