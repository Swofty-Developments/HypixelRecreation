package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.owl;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.OWL, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits a Fann's training session EXP system")
public final class EfficientTrainerAbility implements PetAbility {
    private static final RarityValue<Double> EXP_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0);
    private static final RarityValue<Double> EXP_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.099, 0.0, 0.0);

    @Override
    public String getName() {
        return "Efficient Trainer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(EXP_BASE.getForRarity(rarity)
                + EXP_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Makes training sessions at",
                "<7>Fann more efficient when added",
                "<7>into a session.",
                "",
                "<7>Increased EXP: <b>+" + percent + "% EXP"
        );
    }
}
