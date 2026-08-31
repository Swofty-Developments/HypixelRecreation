package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snail;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SNAIL, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Red Sand Minion system")
public final class RedSandEnjoyerAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.1, 0.2, 0.2, 0.3, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Red Sand Enjoyer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<9>Red Sand Minions <7>work <a>" + percent + "% <7>faster",
                "<7>while on your <b>Private Island<7>."
        );
    }
}
