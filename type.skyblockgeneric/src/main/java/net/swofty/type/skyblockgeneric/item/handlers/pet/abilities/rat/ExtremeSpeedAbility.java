package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.RAT, minimumRarity = Rarity.MYTHIC, order = 0,
        implemented = false, notImplementedReason = "awaits a morph-specific speed/fly system")
public final class ExtremeSpeedAbility implements PetAbility {
    @Override
    public String getName() {
        return "Extreme Speed";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>The Rat is <e>TWO <7>times faster",
                "<7>AND can <e>fly<7>!"
        );
    }
}
