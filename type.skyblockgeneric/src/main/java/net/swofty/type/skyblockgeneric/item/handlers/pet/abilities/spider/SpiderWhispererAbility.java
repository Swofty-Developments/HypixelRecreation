package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spider;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPIDER, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a MinionTick event + minion speed hook")
public final class SpiderWhispererAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Spider Whisperer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = SPEED_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Spider, Cave Spider and Tarantula",
                "<7>minions work <a>" + decimalify(value, 1) + "% <7>faster while on",
                "<7>your island."
        );
    }
}
