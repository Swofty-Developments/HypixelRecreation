package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.kuudra;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.KUUDRA, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Vanquisher spawn system")
public final class WitherBaitAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.1, 0.15, 0.15, 0.2, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Wither Bait";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases the odds of finding",
                "<7>a Vanquisher by <a>" + chance + "%<7>."
        );
    }
}
