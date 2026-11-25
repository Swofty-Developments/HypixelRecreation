package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.scatha;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SCATHA, minimumRarity = Rarity.EPIC,
        implemented = false, notImplementedReason = "awaits Drill items")
public final class DrillInfusionAbility implements PetAbility {
    private static final RarityValue<Double> GEMSTONE_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 1.0, 1.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Drill Infusion";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = decimalify(GEMSTONE_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <6>+" + fortune + " <stat:gemstone_fortune> <7>to",
                "<7>Drills."
        );
    }
}
