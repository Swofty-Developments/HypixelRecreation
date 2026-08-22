package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rabbit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.RABBIT, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a MinionTick event + Farming minion speed hook")
public final class EfficientFarmingAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Efficient Farming";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<6>Farming minions <7>work <a>" + percent + "%<7>",
                "<7>faster while on your island."
        );
    }
}
