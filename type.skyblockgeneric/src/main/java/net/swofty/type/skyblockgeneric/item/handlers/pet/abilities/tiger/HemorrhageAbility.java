package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tiger;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TIGER, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a mob healing-reduction effect")
public final class HemorrhageAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.55, 0.55, 0.0, 0.0);

    @Override
    public String getName() {
        return "Hemorrhage";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Melee attacks reduce healing",
                "<7>by <6>" + decimalify(value, 1) + "% <7>for <a>10s<7>."
        );
    }

    @PetEventHandler
    public void onMeleeDamageDealt(PetEvent.MeleeDamageDealt event) {
    }
}
