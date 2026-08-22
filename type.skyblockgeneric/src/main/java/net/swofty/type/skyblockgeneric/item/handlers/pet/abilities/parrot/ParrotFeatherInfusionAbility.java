package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.parrot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PARROT, minimumRarity = Rarity.LEGENDARY, order = 3,
        implemented = false, notImplementedReason = "awaits a God Potion system (duration boost on consume)")
public final class ParrotFeatherInfusionAbility implements PetAbility {
    private static final RarityValue<Double> DURATION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Parrot Feather Infusion";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DURATION_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>When summoned or in your pets",
                "<7>menu, boost the duration of",
                "<7>consumed <c>God Potions <7>by <a>+" + percent + "%<7>."
        );
    }
}
