package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.guardian;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GUARDIAN, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "no animation yet;awaits a periodic Tick/Pulse event + enemy targeting")
public final class LazerbeamAbility implements PetAbility {
    private static final RarityValue<Double> INTELLIGENCE_MULTIPLIER_PER_LEVEL =
            new RarityValue<>(0.02, 0.06, 0.1, 0.15, 0.2, 1.2, 0.0);

    @Override
    public String getName() {
        return "Lazerbeam";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String multiplier = decimalify(INTELLIGENCE_MULTIPLIER_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Zaps your enemies for <b>" + multiplier + "x",
                "<stat:intelligence> <7>every <a>3s<7>."
        );
    }
}
