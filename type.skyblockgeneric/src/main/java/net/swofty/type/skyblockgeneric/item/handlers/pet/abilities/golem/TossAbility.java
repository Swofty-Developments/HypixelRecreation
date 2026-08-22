package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golem;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GOLEM, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits pet-dealt separate damage instance + damage-type indicator system")
public final class TossAbility implements PetAbility {

    @Override
    public String getName() {
        return "Toss";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Every 5 hits, throw the enemy up into",
                "<7>the air and deal <a>5x <7>damage <8>(5s cooldown)<7>."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {
    }
}
