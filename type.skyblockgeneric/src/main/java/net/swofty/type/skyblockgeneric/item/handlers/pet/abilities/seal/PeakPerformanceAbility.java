package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.seal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SEAL, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.FishCaught) + a Treasure Bait item")
public final class PeakPerformanceAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.035, 0.05, 0.05, 0.0, 0.0);

    @Override
    public String getName() {
        return "Peak Performance";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain a <a>" + chance + "% <7>chance to materialize",
                "<7>some <9>Treasure Bait <7>in your",
                "<7>inventory upon catching <6>Treasure<7>.",
                "<7>Materializes <a>Golden Bait <7>instead",
                "<7>during the <9>Year of the Seal<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
