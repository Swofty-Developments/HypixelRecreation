package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tarantula;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TARANTULA, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a ManaCost event carrying the source item to identify Spider/Tarantula/Spirit boots")
public final class EightLegsAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.5, 0.5, 0.5, 0.0);

    @Override
    public String getName() {
        return "Eight Legs";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Decreases the mana cost of",
                "<7>Spider, Tarantula and Spirit",
                "<7>boots by <a>" + decimalify(value, 1) + "%"
        );
    }

    @PetEventHandler
    public void onManaCost(PetEvent.ManaCost event) {
    }
}
