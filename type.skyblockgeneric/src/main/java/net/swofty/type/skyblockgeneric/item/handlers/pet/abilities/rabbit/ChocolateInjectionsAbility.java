package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rabbit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.RABBIT, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits a Chocolate Factory system (production + duplicate Chocolate Rabbit hook)")
public final class ChocolateInjectionsAbility implements PetAbility {
    private static final RarityValue<Double> PRODUCTION_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.01, 0.0);
    private static final RarityValue<Double> PRODUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.0004, 0.0);
    private static final RarityValue<Double> CHOCOLATE_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 1.3, 0.0);
    private static final RarityValue<Double> CHOCOLATE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.32, 0.0);

    @Override
    public String getName() {
        return "Chocolate Injections";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String production = decimalify(PRODUCTION_BASE.getForRarity(rarity) + PRODUCTION_PER_LEVEL.getForRarity(rarity) * level, 4);
        String chocolate = decimalify(CHOCOLATE_BASE.getForRarity(rarity) + CHOCOLATE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases <6>Chocolate Factory <7>production",
                "<7>by <a>+" + production + "x<7>. Duplicate <a>Chocolate",
                "<a>Rabbits <7>that you find grant <6>+" + chocolate + "%",
                "<6>Chocolate<7>."
        );
    }
}
