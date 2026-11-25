package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rose_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.ROSE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 4,
        implemented = false, notImplementedReason = "awaits a level-200 Rose Dragon + maxed Farming Pet detection")
public final class SymbiosisAbility implements PetAbility {
    @Override
    public String getName() {
        return "Symbiosis";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>If you own a level <a>200 Rose Dragon<7>,",
                "<7>grants <6>+3 <stat:farming_fortune> <7>for",
                "<7>every other unique maxed Farming",
                "<7>pet that you own."
        );
    }
}
