package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAT, minimumRarity = Rarity.RARE, order = 1,
        implemented = false, notImplementedReason = "Night Vision cleanup would strip the effect granted by potions/other gear")
public final class NightmareAbility implements PetAbility {
    private static final RarityValue<Double> INTELLIGENCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.2, 0.3, 0.3, 0.3, 0.0);
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.4, 0.5, 0.5, 0.5, 0.0);

    @Override
    public String getName() {
        return "Nightmare";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double intelligence = INTELLIGENCE_PER_LEVEL.getForRarity(rarity) * level;
        double speed = SPEED_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>During night, gain <a>" + decimalify(intelligence, 2) + " <stat:intelligence><7>,",
                "<a>" + decimalify(speed, 2) + " <stat:speed><7>, and <a>Night Vision<7>."
        );
    }
}
