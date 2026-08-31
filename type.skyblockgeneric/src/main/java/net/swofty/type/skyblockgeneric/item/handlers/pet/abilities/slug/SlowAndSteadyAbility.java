package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.slug;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SLUG, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.FishCaught) + a Crimson Isle region check + a Slugfish catch-timing pipeline")
public final class SlowAndSteadyAbility implements PetAbility {
    private static final RarityValue<Double> CATCH_TIME_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Slow and Steady";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(CATCH_TIME_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>When fishing in the <c>Crimson Isle<7>,",
                "<a>Slugfish <7>take <a>" + percent + "% <7>less time to catch."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
