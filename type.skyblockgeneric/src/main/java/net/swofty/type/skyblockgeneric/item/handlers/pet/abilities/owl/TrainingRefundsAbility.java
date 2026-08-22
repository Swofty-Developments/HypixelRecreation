package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.owl;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.OWL, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a Fann's training session system (coin cost)")
public final class TrainingRefundsAbility implements PetAbility {
    @Override
    public String getName() {
        return "Training Refunds";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>The more coins to spend on",
                "<7>Fann's Sessions, the less coins",
                "<7>they will cost. <8>(max 5% off)."
        );
    }
}
