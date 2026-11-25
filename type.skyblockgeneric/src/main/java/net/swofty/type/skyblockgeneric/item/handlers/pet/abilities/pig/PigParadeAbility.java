package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.pig;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.PIG, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Year of the Pig festival system (calendar event)")
public final class PigParadeAbility implements PetAbility {
    private static final RarityValue<Double> BASE_STAT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Pig Parade";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = commaify(BASE_STAT_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Increases the base stats of this",
                "<7>pet by <a>" + percent + "% <7>during the <d>Year",
                "<7>of the Pig<7>."
        );
    }
}
