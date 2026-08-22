package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.witch;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.WITCH, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits the Year of the Witch event + an ingredient drop system")
public final class ToilAndTroubleAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.5, 0.75, 0.75, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Toil and Trouble";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases your chance of dropping",
                "<6>Ingredients <7>during the <5>Year of the",
                "<5>Witch <7>by <a>" + decimalify(value, 1) + "%<7>."
        );
    }
}
