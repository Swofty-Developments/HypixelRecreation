package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jerry;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.JERRY, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a per-item stat augmentation system")
public final class JerryAspectAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.1, 0.5, 0.0);

    @Override
    public String getName() {
        return "Jerry";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String damage = decimalify(DAMAGE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Actually adds <c>" + damage + " damage <7>to",
                "<7>the Aspect of the Jerry."
        );
    }
}
