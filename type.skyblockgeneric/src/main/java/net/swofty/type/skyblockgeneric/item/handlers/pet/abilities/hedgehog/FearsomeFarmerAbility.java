package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hedgehog;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;
import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HEDGEHOG, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits a Pest system")
public final class FearsomeFarmerAbility implements PetAbility {
    private static final double FARMING_FORTUNE_PER_LEVEL = 1;
    private static final double OVERBLOOM_PER_LEVEL = 0.35;

    @Override
    public String getName() {
        return "Fearsome Farmer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double farmingFortune = FARMING_FORTUNE_PER_LEVEL * level;
        double overbloom = OVERBLOOM_PER_LEVEL * level;

        return List.of(
                "<7>Grants <6>+" + commaify(farmingFortune) + " <stat:farming_fortune> <7>and",
                "<e>+" + decimalify(overbloom, 2) + " <stat:overbloom> <7>on <2>Pests<7>."
        );
    }
}
