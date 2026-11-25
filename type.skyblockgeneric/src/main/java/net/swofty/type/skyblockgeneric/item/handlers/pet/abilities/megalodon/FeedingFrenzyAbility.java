package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.megalodon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MEGALODON, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Fishing Festival system + FishCaught dispatch in FishingLootResolver")
public final class FeedingFrenzyAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0);
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.0);

    @Override
    public String getName() {
        return "Feeding Frenzy";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_BASE.getForRarity(rarity)
                + CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants a <a>" + chance + "% <7>chance to catch",
                "<b>Sharks <7>during the <b>Fishing Festival<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {
    }
}
