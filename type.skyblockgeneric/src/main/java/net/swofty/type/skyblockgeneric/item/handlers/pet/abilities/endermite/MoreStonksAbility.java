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

@PetAbilityRegistration(pet = PetHandler.ENDERMITE, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits dispatch(PetEvent.BlockMined) hook + exp orb's type need to be confirmed")
public final class MoreStonksAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.5, 0.8, 0.8, 1.0, 1.0, 1.0, 0.0);

    @Override
    public String getName() {
        return "More Stonks";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain more exp orbs for",
                "<7>breaking end stone and gain a",
                "<7>+<a>" + decimalify(value, 2) + "% <7>chance to get an extra",
                "<7>block dropped."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {

    }
}
