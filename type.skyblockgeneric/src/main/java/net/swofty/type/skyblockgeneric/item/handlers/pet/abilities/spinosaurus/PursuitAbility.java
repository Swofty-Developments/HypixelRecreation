package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spinosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SPINOSAURUS, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits a trophy-fish tier system + trophy-catch hook")
public final class PursuitAbility implements PetAbility {
    private static final double PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Pursuit";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increases the chance of catching",
                "<6><l>GOLD<r><7> and <b><l>DIAMOND<r><7> tier <6>Trophy",
                "<6>Fish <7>by <a>" + decimalify(value, 1) + "%<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {
    }
}
