package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mole;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MOLE, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a Mines of Divan scavenged item drop system")
public final class ArchaeologistAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Archaeologist";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increase your chance of finding",
                "<c>Scavenged Items <7>in the <2>Mines of",
                "<2>Divan <7>by <a>" + chance + "%<7>."
        );
    }
}
