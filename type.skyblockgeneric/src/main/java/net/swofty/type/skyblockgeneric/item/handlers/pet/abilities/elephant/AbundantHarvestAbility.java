package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.elephant;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ELEPHANT, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.CropHarvested) hook")
public final class AbundantHarvestAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.0);

    @Override
    public String getName() {
        return "Abundant Harvest";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Earn <2>+" + decimalify(value, 1) + "% Sowdust <7>while farming."
        );
    }

    @PetEventHandler
    public void onCropHarvested(PetEvent.CropHarvested event) {

    }
}
