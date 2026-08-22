package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rock;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.ROCK, minimumRarity = Rarity.COMMON, order = 1,
        implemented = false, notImplementedReason = "awaits a pet relocation system (sneak-to-teleport)")
public final class SailingStoneAbility implements PetAbility {
    @Override
    public String getName() {
        return "Sailing Stone";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Sneak to move your rock to your",
                "<7>location <8>(15s cooldown)<7>."
        );
    }
}
