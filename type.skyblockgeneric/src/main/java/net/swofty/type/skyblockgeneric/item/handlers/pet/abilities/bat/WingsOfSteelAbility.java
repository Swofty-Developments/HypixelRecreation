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

@PetAbilityRegistration(pet = PetHandler.BAT, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Spooky Festival / Spooky enemy system")
public final class WingsOfSteelAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.5, 0.0);

    @Override
    public String getName() {
        return "Wings of Steel";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = DAMAGE_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Deals <a>+" + decimalify(value, 2) + "% <7>damage to <6>Spooky",
                "<7>enemies during the <6>Spooky Festival<7>."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {

    }
}
