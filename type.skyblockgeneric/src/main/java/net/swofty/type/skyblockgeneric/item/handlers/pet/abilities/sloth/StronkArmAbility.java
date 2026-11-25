package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sloth;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SLOTH, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits an Axe-throw mechanic")
public final class StronkArmAbility implements PetAbility {
    private static final RarityValue<Double> SWEEP_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.2, 0.4, 0.4, 0.0, 0.0);
    private static final RarityValue<Double> FORAGING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.5, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Stronk Arm";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String sweep = decimalify(SWEEP_PER_LEVEL.getForRarity(rarity) * level, 1);
        String foraging = decimalify(FORAGING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Gains <2>+" + sweep + " <stat:sweep> <7>and",
                "<6>+" + foraging + " <stat:foraging_fortune> <7>on <2>Axe <7>throws."
        );
    }
}
