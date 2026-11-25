package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.seal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SEAL, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Year of the Seal flag")
public final class AmphibiousAbility implements PetAbility {
    private static final RarityValue<Double> BASE_STATS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Amphibious";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String stats = decimalify(BASE_STATS_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Increases the base stats of this pet",
                "<7>by <a>" + stats + "% <7>during the <9>Year of the",
                "<9>Seal<7>."
        );
    }
}
