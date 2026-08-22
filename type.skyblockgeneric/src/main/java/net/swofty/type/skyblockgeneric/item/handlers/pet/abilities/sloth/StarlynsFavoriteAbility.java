package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.sloth;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SLOTH, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Starlyn Contests system")
public final class StarlynsFavoriteAbility implements PetAbility {
    private static final RarityValue<Double> POINTS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Starlyn's Favorite";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String points = decimalify(POINTS_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Cutting <a>Trees <7>give <a>+" + points + "% <7>more points",
                "<7>towards <d>Starlyn Contests<7>."
        );
    }
}
