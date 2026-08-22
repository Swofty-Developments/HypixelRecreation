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

@PetAbilityRegistration(pet = PetHandler.ENDERMITE, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "Transmission Abilities contains what?")
public final class DailyCommuterAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.4, 0.4, 0.4, 0.0);

    @Override
    public String getName() {
        return "Daily Commuter";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<9>Transmission Abilities",
                "<7>cost <a>" + decimalify(value, 2) + "% <7>less mana."
        );
    }

    @PetEventHandler
    public void onManaCost(PetEvent.ManaCost event) {

    }
}
