package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mosquito;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOSQUITO, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a Garden visitor system (unique visitors served)")
public final class BuzzinBartererAbility implements PetAbility {
    private static final RarityValue<Double> SUGAR_CANE_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.01, 0.02, 0.02, 0.0, 0.0);

    @Override
    public String getName() {
        return "Buzzin' Barterer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(SUGAR_CANE_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <6>+" + value + " <stat:sugar_cane_fortune> <7>for",
                "<7>every unique visitor you've served",
                "<7>in <a>The Garden<7>.",
                "<8>Capped at 175 Fortune"
        );
    }
}
