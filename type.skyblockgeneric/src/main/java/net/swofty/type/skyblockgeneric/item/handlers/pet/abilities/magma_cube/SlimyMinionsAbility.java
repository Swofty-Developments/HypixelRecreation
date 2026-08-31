package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.magma_cube;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MAGMA_CUBE, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a MinionTick event + minion speed hook")
public final class SlimyMinionsAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.2, 0.2, 0.25, 0.3, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Slimy Minions";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Slime and Magma Cube minions work",
                "<a>" + percent + "% <7>faster while on your island."
        );
    }
}
