package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.pig;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PIG, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Shiny Pig coin-drop system")
public final class HamfistedAbility implements PetAbility {
    private static final RarityValue<Double> COIN_PER_LEVEL =
            new RarityValue<>(0.2, 0.35, 0.35, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Hamfisted";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(COIN_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases <6>Coin <7>gain from",
                "<6>Shiny Pigs <7>by <a>+" + percent + "%<7>."
        );
    }
}
