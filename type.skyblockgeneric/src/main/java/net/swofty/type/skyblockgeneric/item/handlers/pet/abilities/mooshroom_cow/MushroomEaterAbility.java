package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mooshroom_cow;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOOSHROOM_COW, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits CropHarvested dispatch in the farming crop harvest + a Mushroom drop handler")
public final class MushroomEaterAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.5, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Mushroom Eater";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>You have a <a>+" + chance + "% <7>chance to drop a",
                "<7>Mushroom when farming crops."
        );
    }

    @PetEventHandler
    public void onCropHarvested(PetEvent.CropHarvested event) {
    }
}
