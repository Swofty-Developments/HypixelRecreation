package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.reindeer;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.REINDEER, minimumRarity = Rarity.LEGENDARY, order = 3,
        implemented = false, notImplementedReason = "awaits an Ice Essence drop system (double-drop hook)")
public final class IcyWindAbility implements PetAbility {
    private static final RarityValue<Double> DOUBLE_CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Icy Wind";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DOUBLE_CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <a>+" + percent + "% <7>chance of",
                "<7>getting double <b>Ice Essence<7>."
        );
    }
}
