package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.endermite;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDERMITE, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.BlockMined) hook + a Nest Endermite mob")
public final class MiteBaitAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.03, 0.03, 0.0);

    @Override
    public String getName() {
        return "Mite Bait";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain a <a>" + decimalify(value, 2) + "% <7>chance to dig up",
                "<7>a bonus <c>Nest Endermite <7>per",
                "<d>+1 <stat:pet_luck> <8>(Stacks above",
                "<8>100%)."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {

    }
}
