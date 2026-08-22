package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.precursor_drone;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PRECURSOR_DRONE, minimumRarity = Rarity.COMMON, order = 0,
        implemented = false, notImplementedReason = "awaits FishCaught dispatch in FishingLootResolver + a Treasure drop system")
public final class ContrabandAbility implements PetAbility {
    @Override
    public String getName() {
        return "Contraband";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Catching a <3>Sea Creature <7>has a",
                "<a>10% <7>chance to also give you",
                "<6>Treasure<7>."
        );
    }

    @PetEventHandler
    public void onFishCaught(PetEvent.FishCaught event) {
    }
}
