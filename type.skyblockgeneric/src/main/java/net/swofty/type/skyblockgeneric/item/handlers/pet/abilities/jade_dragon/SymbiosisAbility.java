package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jade_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JADE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 4,
        implemented = false, notImplementedReason = "awaits a level-200 Jade Dragon + maxed Foraging Pet detection")
public final class SymbiosisAbility implements PetAbility {

    @Override
    public String getName() {
        return "Symbiosis";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>If you own a level <a>200 Jade Dragon<7>,",
                "<7>Grants <6>+4 <stat:foraging_fortune> <7>for every",
                "<7>other unique maxed Foraging Pet that you own."
        );
    }
}
