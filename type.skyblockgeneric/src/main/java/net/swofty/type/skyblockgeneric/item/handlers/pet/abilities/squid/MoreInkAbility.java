package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.squid;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SQUID, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a LootRoll drop-doubling hook on squid kills")
public final class MoreInkAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.5, 0.75, 0.75, 1.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "More Ink";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain a <a>" + decimalify(value, 1) + "% <7>chance to get",
                "<7>double drops from squids."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob event) {
    }
}
