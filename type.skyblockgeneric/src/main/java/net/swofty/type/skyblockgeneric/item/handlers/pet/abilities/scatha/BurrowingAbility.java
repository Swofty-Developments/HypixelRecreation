package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.scatha;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SCATHA, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a treasure-chest discovery system while mining")
public final class BurrowingAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.4, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Burrowing";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants a <a>+" + chance + "% <7>chance to find",
                "<e>Treasure Chests <7>while mining."
        );
    }
}
