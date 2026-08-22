package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.slug;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SLUG, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Sprayonator plot system")
public final class RepugnantAromaAbility implements PetAbility {
    private static final RarityValue<Double> FARMING_FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Repugnant Aroma";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = decimalify(FARMING_FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>When farming in a plot affected by a",
                "<a>Sprayonator<7>, gain <6>+" + fortune + " <stat:farming_fortune><7>."
        );
    }
}
