package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jerry;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.JERRY, minimumRarity = Rarity.MYTHIC, order = 3,
        implemented = false, notImplementedReason = "awaits a Jerry Candy item")
public final class JerryCandyAbility implements PetAbility {

    @Override
    public String getName() {
        return "Jerry";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Tiny chance to find Jerry",
                "<7>Candies when killing mobs."
        );
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {

    }
}
