package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.turtle;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TURTLE, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits an ally-aura system granting Defense to nearby players")
public final class GeniusAmnioteAbility implements PetAbility {
    private static final double DEFENSE_BASE = 1.0;
    private static final RarityValue<Double> DEFENSE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.015, 0.015, 0.0, 0.0);

    @Override
    public String getName() {
        return "Genius Amniote";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = DEFENSE_BASE + DEFENSE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <a>+" + decimalify(value, 2) + "% <stat:defense> <7>to",
                "<a>4 <7>players within <a>50 <7>blocks of you."
        );
    }
}
