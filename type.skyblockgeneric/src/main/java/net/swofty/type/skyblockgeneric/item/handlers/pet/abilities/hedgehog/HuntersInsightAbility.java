package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hedgehog;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HEDGEHOG, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Pest Bestiary system")
public final class HuntersInsightAbility implements PetAbility {
    private static final double FARMING_FORTUNE_PER_TIER = 0.7;

    @Override
    public String getName() {
        return "Hunter's Insight";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants <6>+" + decimalify(FARMING_FORTUNE_PER_TIER, 1) + " <stat:farming_fortune>",
                "<7>per <7>Pest Bestiary Tier."
        );
    }
}
