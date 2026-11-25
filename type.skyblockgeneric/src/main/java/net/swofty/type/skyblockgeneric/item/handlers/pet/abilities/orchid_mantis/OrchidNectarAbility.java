package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.orchid_mantis;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ORCHID_MANTIS, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits CropHarvested dispatch + a Jelly/Plant Matter drop system")
public final class OrchidNectarAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0002, 0.0, 0.0);

    @Override
    public String getName() {
        return "Orchid Nectar";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>You have a <a>+" + chance + "% <7>chance to find",
                "<a>Jelly <7>or <a>Plant Matter <7>when farming",
                "<7>crops."
        );
    }

    @PetEventHandler
    public void onCropHarvested(PetEvent.CropHarvested event) {
    }
}
