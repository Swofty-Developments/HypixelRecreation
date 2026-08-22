package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.scatha;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SCATHA, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Gemstone Powder gain pipeline (PetEvent.PowderGain)")
public final class BejeweledEyesAbility implements PetAbility {
    private static final RarityValue<Double> POWDER_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Bejeweled Eyes";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String powder = decimalify(POWDER_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Earn <a>+" + powder + "% <d>Gemstone Powder <7>from",
                "<7>all sources."
        );
    }
}
