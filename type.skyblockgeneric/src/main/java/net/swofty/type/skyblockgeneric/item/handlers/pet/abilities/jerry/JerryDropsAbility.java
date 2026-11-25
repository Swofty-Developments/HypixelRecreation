package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jerry;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JERRY, minimumRarity = Rarity.COMMON, order = 1)
public final class JerryDropsAbility implements PetAbility {

    @Override
    public String getName() {
        return "Jerry";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Gain <a>100% <7>chance to",
                "<7>receive a normal amount of",
                "<7>drops from mobs."
        );
    }
}
