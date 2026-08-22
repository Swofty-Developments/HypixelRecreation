package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAL, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a Crystal Hollows Heat system; Kill hook already dispatched")
public final class DispersionAbility implements PetAbility {
    private static final RarityValue<Double> HEAT_REDUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.04, 0.04, 0.0, 0.0);

    @Override
    public String getName() {
        return "Dispersion";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = HEAT_REDUCTION_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>While in the <5>Crystal Hollows<7>, killing",
                "<7>mobs reduces your <c>Heat <7>by <c>" + decimalify(value, 2) + "<7>."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob event) {

    }
}
