package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spider;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPIDER, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits an Arachnal item tag/category on weapons/armor/equipment")
public final class OneWithTheSpiderAbility implements PetAbility {
    private static final RarityValue<Double> BASE =
            new RarityValue<>(1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 0.0);
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.02, 0.04, 0.06, 0.08, 0.1, 0.1, 0.0);

    @Override
    public String getName() {
        return "One with the Spider";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE.getForRarity(rarity) + PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Applies <c>" + decimalify(value, 1) + " <stat:strength> <7>to all",
                "<4>Arachnal <7>weapons, armor, and",
                "<7>equipment you have equipped."
        );
    }
}
