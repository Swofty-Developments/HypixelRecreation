package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.witch;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WITCH, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a brewing system")
public final class AlchemismAbility implements PetAbility {
    private static final RarityValue<Double> REDUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.4, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Alchemism";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = REDUCTION_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Reduces how long <5>Potions <7>take to",
                "<7>brew by <a>" + decimalify(value, 1) + "%<7>."
        );
    }
}
