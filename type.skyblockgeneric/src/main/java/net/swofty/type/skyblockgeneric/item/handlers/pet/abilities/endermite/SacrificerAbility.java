package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.endermite;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDERMITE, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits a Draconic Altar system")
public final class SacrificerAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0);

    @Override
    public String getName() {
        return "Sacrificer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases the odds of rolling",
                "<7>for bonus items in the",
                "<c>Draconic Altar <7>by <a>" + decimalify(value, 2) + "%<7>."
        );
    }
}
