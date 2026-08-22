package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.bal;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BAL, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a Bal boss mob")
public final class CannibalAbility implements PetAbility {
    private static final double PER_LEVEL = 1;

    @Override
    public String getName() {
        return "Cannibal";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increases damage dealt to <c><l>Bal<r> <7>by",
                "<a>" + decimalify(value, 1) + "%<7>."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {

    }
}
