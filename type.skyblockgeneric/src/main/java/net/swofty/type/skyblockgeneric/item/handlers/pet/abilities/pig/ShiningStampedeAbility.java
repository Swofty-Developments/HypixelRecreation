package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.pig;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PIG, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a Shiny Pig Bestiary system")
public final class ShiningStampedeAbility implements PetAbility {
    private static final RarityValue<Double> POTATO_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.04, 0.05, 0.05, 0.0, 0.0);

    @Override
    public String getName() {
        return "Shining Stampede";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = decimalify(POTATO_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <6>+" + fortune + " <stat:potato_fortune> <7>per",
                "<6>Shiny Pig <3>Bestiary <7>tier."
        );
    }
}
