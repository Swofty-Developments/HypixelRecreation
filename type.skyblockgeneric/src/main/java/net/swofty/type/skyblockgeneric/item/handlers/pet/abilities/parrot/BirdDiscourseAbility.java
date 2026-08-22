package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.parrot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PARROT, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a player aura system (grants Strength to the owner and all players within 20 blocks, regardless of pet ownership)")
public final class BirdDiscourseAbility implements PetAbility {
    private static final RarityValue<Double> STRENGTH_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.25, 0.0, 0.0);

    @Override
    public String getName() {
        return "Bird Discourse";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String strength = decimalify(STRENGTH_BASE.getForRarity(rarity)
                + STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gives <c>+" + strength + " <stat:strength> <7>to",
                "<7>players within <a>20 <7>blocks <8>(doesn't",
                "<8>stack)<7>."
        );
    }
}
