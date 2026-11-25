package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rock;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ROCK, minimumRarity = Rarity.RARE, order = 0,
        implemented = false, notImplementedReason = "awaits a ride/mount state system")
public final class FortifyAbility implements PetAbility {
    private static final RarityValue<Double> DEFENSE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.2, 0.25, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Fortify";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DEFENSE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>While sitting on your rock, gain",
                "<a>+" + percent + "% <7>defense."
        );
    }
}
