package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.reindeer;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.REINDEER, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Gift Attack event system")
public final class SnowPowerAbility implements PetAbility {
    private static final RarityValue<Double> GIFT_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Snow Power";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(GIFT_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <a>+" + percent + "% <7>bonus gift",
                "<7>chance during the <c>Gift Attack<7>",
                "<c>event<7>."
        );
    }
}
