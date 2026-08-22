package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.penguin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.PENGUIN, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits FishCaught dispatch in FishingLootResolver + a Cold system")
public final class ThickBlubberAbility implements PetAbility {
    private static final RarityValue<Double> COLD_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);
    private static final RarityValue<Double> COLD_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.05, 0.0, 0.0);

    @Override
    public String getName() {
        return "Thick Blubber";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String cold = commaify(Math.floor(COLD_BASE.getForRarity(rarity)
                + COLD_PER_LEVEL.getForRarity(rarity) * level));

        return List.of(
                "<7>Each time you catch a <3>Sea",
                "<7>Creature<7>, reduce your <b>Cold <7>by",
                "<a>" + cold + "<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {
    }
}
