package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mammoth;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MAMMOTH, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Frozen Corpse loot system in Glacite Mineshafts")
public final class CorpseCrusherAbility implements PetAbility {
    private static final RarityValue<Double> MINING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Corpse Crusher";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(MINING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Gain <6>+" + value + " <stat:mining_fortune> <7>for each",
                "<b>Frozen Corpse <7>looted in your",
                "<7>current <b>Glacite Mineshaft<7>."
        );
    }
}
