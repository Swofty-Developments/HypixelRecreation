package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.guardian;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GUARDIAN, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits a Superpairs (Experiment Table) system")
public final class LuckySevenAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.07, 0.0);

    @Override
    public String getName() {
        return "Lucky Seven";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <b>+" + chance + "% <7>chance to find",
                "<5>ultra rare <7>books in <d>Superpairs<7>."
        );
    }
}
