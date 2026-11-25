package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.lion;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.LION, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a per-item stat augmentation system for weapons")
public final class PrimalForceAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.03, 0.05, 0.1, 0.15, 0.2, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.03, 0.05, 0.1, 0.15, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Primal Force";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String damage = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 2);
        String strength = decimalify(STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Adds <c>+" + damage + " <stat:damage> <7>and",
                "<7><c>+" + strength + " <stat:strength> <7>to your",
                "<7>weapons."
        );
    }
}
