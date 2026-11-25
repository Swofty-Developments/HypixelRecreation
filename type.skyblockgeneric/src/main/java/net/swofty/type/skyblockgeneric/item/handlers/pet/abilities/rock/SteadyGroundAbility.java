package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rock;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ROCK, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a ride/mount state system")
public final class SteadyGroundAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Steady Ground";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String multiplier = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>While sitting on your rock, gain",
                "<c>+" + multiplier + "x <7>damage."
        );
    }
}
