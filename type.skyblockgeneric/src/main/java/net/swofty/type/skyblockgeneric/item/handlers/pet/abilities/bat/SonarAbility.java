package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bat;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAT, minimumRarity = Rarity.MYTHIC, order = 3,
        implemented = false, notImplementedReason = "awaits FishCaught dispatch + a Spooky Festival system")
public final class SonarAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.0);

    @Override
    public String getName() {
        return "Sonar";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants a <a>+" + decimalify(value, 2) + "% <7>chance to catch",
                "<6>Spooky Sea Creatures <7>during the",
                "<6>Spooky Festival<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {

    }
}
