package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rabbit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.RABBIT, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a PotionEffectApplied event + a Jump potion effect hook")
public final class HappyFeetAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.3, 0.4, 0.4, 0.5, 0.5, 0.5, 0.0);

    @Override
    public String getName() {
        return "Happy Feet";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String speed = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Jump potions also give <a>+" + speed,
                "<stat:speed><7>."
        );
    }
}
