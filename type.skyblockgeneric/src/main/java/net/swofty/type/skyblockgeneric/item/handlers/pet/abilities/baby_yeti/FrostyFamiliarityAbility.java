package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.baby_yeti;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BABY_YETI, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "Magic Find vs Winter Sea Creatures needs a fishing-loot hook; FishCaught not dispatched")
public final class FrostyFamiliarityAbility implements PetAbility {
    private static final RarityValue<Double> MAGIC_FIND_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.1, 0.1, 0.0);

    @Override
    public String getName() {
        return "Frosty Familiarity";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = MAGIC_FIND_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <b>+" + decimalify(value, 2) + " <stat:magic_find> <7>against",
                "<f>Winter Sea Creatures<7>."
        );
    }
}
