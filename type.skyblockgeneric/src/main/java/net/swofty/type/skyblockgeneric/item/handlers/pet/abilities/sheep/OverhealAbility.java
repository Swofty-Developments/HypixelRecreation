package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sheep;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SHEEP, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a shield mechanic")
public final class OverhealAbility implements PetAbility {
    private static final RarityValue<Double> SHIELD_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.1, 0.1, 0.1, 0.0, 0.0);

    @Override
    public String getName() {
        return "Overheal";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String shield = decimalify(SHIELD_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Gives a <a>" + shield + "% <7>shield after",
                "<7>not taking damage for 10s."
        );
    }
}
