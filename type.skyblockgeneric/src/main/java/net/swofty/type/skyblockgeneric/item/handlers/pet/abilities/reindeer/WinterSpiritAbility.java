package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.reindeer;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.REINDEER, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a pet EXP gain hook (unified XP pipeline)")
public final class WinterSpiritAbility implements PetAbility {
    @Override
    public String getName() {
        return "Winter Spirit";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Gain <d>double <7>pet <a>EXP<7>."
        );
    }
}
