package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hermit_crab;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HERMIT_CRAB, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits a Hotspot fishing system + dispatch(PetEvent.FishCaught)")
public final class HotspotHazardAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.0);

    @Override
    public String getName() {
        return "Hotspot Hazard";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Increases the chance of catching",
                "<d>Hotspot Sea Creatures <7>by <a>" + chance + "%<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
