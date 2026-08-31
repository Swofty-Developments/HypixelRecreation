package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.flying_fish;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.FLYING_FISH, minimumRarity = Rarity.LEGENDARY, maximumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits Diver Armor + Abyssal Armor items and an armor-stat upgrade system")
public final class DeepSeaDiverAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Deep Sea Diver";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases the stats of <a>Diver Armor",
                "<7>and <a>Abyssal Armor <7>by <a>" + decimalify(value, 2) + "%<7>"
        );
    }
}
