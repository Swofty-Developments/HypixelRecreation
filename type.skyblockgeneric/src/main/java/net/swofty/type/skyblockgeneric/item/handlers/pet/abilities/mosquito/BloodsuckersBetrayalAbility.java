package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mosquito;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOSQUITO, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Pest system (Pest Trap collection hook)")
public final class BloodsuckersBetrayalAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Bloodsucker's Betrayal";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>When collected, <2>Pest Traps <7>will catch",
                "<7>the next pest <a>" + percent + "% <7>faster."
        );
    }
}
