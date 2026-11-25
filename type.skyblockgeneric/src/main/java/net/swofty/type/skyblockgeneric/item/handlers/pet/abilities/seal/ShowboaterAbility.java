package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.seal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SEAL, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Year of the Seal flag + Bouncy Beach Ball catches")
public final class ShowboaterAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.5, 0.75, 0.75, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Showboater";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases your chance of catching",
                "<5>Bouncy Beach Balls <7>and <6>Giant Bouncy",
                "<6>Beach Balls <7>during the <9>Year of the",
                "<9>Seal <7>by <a>" + chance + "%<7>."
        );
    }
}
