package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.glacite_golem;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.GLACITE_GOLEM, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a Glacite Mineshafts region; region-gated MINING_FORTUNE stat")
public final class IcebornAbility implements PetAbility {
    private static final RarityValue<Double> MINING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.75, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Iceborn";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = commaify(MINING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level);

        return List.of(
                "<7>Gain <a>+" + value + " <stat:mining_fortune> <7>while in the",
                "<b>Glacite Mineshafts<7>."
        );
    }
}
