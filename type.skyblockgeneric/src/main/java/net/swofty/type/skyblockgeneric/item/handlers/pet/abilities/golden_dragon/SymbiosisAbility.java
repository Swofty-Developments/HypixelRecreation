package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golden_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GOLDEN_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 4,
        implemented = false, notImplementedReason = "awaits a level-200 Golden Dragon + maxed Combat Pet detection")
public final class SymbiosisAbility implements PetAbility {
    @Override
    public String getName() {
        return "Symbiosis";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>If you own a level <a>200 Golden Dragon<7>, gain",
                "<6>+5 coins <7>per monster kill for every other",
                "<7>unique maxed Combat Pet that you own."
        );
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {

    }
}
