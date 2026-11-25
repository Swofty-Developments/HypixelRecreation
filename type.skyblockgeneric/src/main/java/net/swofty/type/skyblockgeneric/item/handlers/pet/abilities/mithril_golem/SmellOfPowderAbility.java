package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mithril_golem;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MITHRIL_GOLEM, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a PowderGain event + Mithril Powder gain hook")
public final class SmellOfPowderAbility implements PetAbility {
    private static final RarityValue<Double> POWDER_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "The Smell Of Powder";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(POWDER_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <2>+" + percent + "% <7>Mithril Powder <7>from",
                "<7>all sources."
        );
    }
}
