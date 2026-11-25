package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAT, minimumRarity = Rarity.COMMON, order = 0,
        implemented = false, notImplementedReason = "awaits a candy drop system; Kill hook already dispatched")
public final class CandyLoverAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.1, 0.15, 0.15, 0.2, 0.2, 0.2, 0.0);

    @Override
    public String getName() {
        return "Candy Lover";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Increases drop chance of candies",
                "<7>from mobs by <a>" + decimalify(value, 2) + "%<7>."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob event) {

    }
}
