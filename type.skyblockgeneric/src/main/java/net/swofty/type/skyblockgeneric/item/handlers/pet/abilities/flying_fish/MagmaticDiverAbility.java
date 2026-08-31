package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.flying_fish;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.FLYING_FISH, minimumRarity = Rarity.MYTHIC, order = 4,
        implemented = false, notImplementedReason = "awaits Diver Armor + Magma Lord Armor + Abyssal Armor items and an armor-stat upgrade system")
public final class MagmaticDiverAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.0);

    @Override
    public String getName() {
        return "Magmatic Diver";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases the stats of Magma",
                "<7>Lord armor by <a>" + decimalify(value, 2) + "%<7>"
        );
    }
}
