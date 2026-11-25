package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.parrot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PARROT, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a PotionEffectApplied event + potion duration hook")
public final class RepeatAbility implements PetAbility {
    private static final RarityValue<Double> DURATION_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 5.0, 5.0, 0.0, 0.0);
    private static final RarityValue<Double> DURATION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.35, 0.35, 0.0, 0.0);

    @Override
    public String getName() {
        return "Repeat";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(DURATION_BASE.getForRarity(rarity)
                + DURATION_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Boosts potions duration by <a>" + percent + "%<7>."
        );
    }
}
