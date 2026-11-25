package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.enderman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDERMAN, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "not Ender mobs right now")
public final class EnderianAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.1, 0.2, 0.2, 0.3, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Enderian";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Take <a>" + decimalify(value, 1) + "% <7>less damage from <5>Ender",
                "<7>mobs"
        );
    }
}
