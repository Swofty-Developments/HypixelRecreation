package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rose_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.ROSE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 3,
        implemented = false, notImplementedReason = "awaits a Garden Visitors / Mutations system")
public final class SpiritualPerfectionAbility implements PetAbility {
    @Override
    public String getName() {
        return "Spiritual Perfection";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Gain <a>20% <7>more <c>Copper <7>from <a>Garden",
                "<a>Visitors<7> and from analyzing <e>Mutations<7>."
        );
    }
}
