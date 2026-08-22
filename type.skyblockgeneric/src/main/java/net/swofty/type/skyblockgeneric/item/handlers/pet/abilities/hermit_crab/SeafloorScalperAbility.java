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

@PetAbilityRegistration(pet = PetHandler.HERMIT_CRAB, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits treasure-quality tiers in CatchPayload + dispatch(PetEvent.FishCaught)")
public final class SeafloorScalperAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.075, 0.1, 0.1, 0.1, 0.0);

    @Override
    public String getName() {
        return "Seafloor Scalper";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String chance = decimalify(CHANCE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<6>Treasure <7>catches are <a>" + chance + "% <7>more",
                "<7>likely to be <6><l>GREAT <7>or <d><l>OUTSTANDING<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
