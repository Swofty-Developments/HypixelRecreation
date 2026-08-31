package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jade_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JADE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 3,
        implemented = false, notImplementedReason = "awaits an Attribute system (maxed attributes)")
public final class ApexPredatorAbility implements PetAbility {

    @Override
    public String getName() {
        return "Apex Predator";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Increases your total <2>Sweep <7>by",
                "<2>0.1% <7>for every Maxed out Attribute",
                "<7>you unlocked."
        );
    }
}
