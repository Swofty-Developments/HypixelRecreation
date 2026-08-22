package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.blaze;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BLAZE, minimumRarity = Rarity.RARE, order = 1,
        implemented = false, notImplementedReason = "awaits a Blaze Armor item + an armor-ability upgrade system")
public final class BlingArmorAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.35, 0.4, 0.4, 0.0, 0.0);

    @Override
    public String getName() {
        return "Bling Armor";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Upgrades <c>Blaze Armor <7>stats",
                "<7>and ability by <a>" + decimalify(value, 2) + "%<7>"
        );
    }
}
