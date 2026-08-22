package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.RAT, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a CheeseFound event + a temporary buff system")
public final class RatsBlessingAbility implements PetAbility {
    private static final RarityValue<Double> MAGIC_FIND_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 2.0, 2.0, 0.0);
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.05, 0.05, 0.0);
    private static final RarityValue<Double> DURATION_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 20.0, 20.0, 0.0);
    private static final RarityValue<Double> DURATION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.4, 0.4, 0.0);

    @Override
    public String getName() {
        return "Rat's Blessing";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String magicFind = decimalify(MAGIC_FIND_BASE.getForRarity(rarity) + MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level, 2);
        String seconds = decimalify(DURATION_BASE.getForRarity(rarity) + DURATION_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Has a chance to grant a random",
                "<7>player <b>+" + magicFind + " <stat:magic_find> <7>for <a>" + seconds,
                "<7>seconds after finding a yummy piece",
                "<7>of Cheese! If the player gets a drop",
                "<7>during this buff, you have a <a>20%",
                "<7>chance to get it too."
        );
    }
}
