package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.lion;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.LION, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits an EnchantmentProc hook (First Strike/Triple-Strike/Combo)")
public final class FirstPounceAbility implements PetAbility {
    private static final RarityValue<Double> EFFECTIVENESS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.75, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "First Pounce";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(EFFECTIVENESS_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>First Strike, Triple-Strike, and",
                "<d><l>Combo<r> <7>are <a>" + percent + "% <7>more",
                "<7>effective."
        );
    }
}
