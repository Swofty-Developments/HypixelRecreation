package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mole;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.MOLE, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Crystal Nucleus completion system")
public final class NucleicExplorerAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Nucleic Explorer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = commaify(CHANCE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain a <a>" + chance + "% <7>chance to receive an",
                "<7>extra drop when completing the",
                "<d>Crystal Nucleus<7>."
        );
    }
}
