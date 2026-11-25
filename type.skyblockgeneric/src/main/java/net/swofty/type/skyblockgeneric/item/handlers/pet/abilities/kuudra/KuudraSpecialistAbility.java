package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.kuudra;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.KUUDRA, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Kuudra boss mob")
public final class KuudraSpecialistAbility implements PetAbility {

    @Override
    public String getName() {
        return "Kuudra Specialist";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Increases all damage to Kuudra and",
                "<7>his minions by <c>20%<7>."
        );
    }

    @PetEventHandler
    public void onDamageDealt(PetEvent.DamageDealt event) {

    }
}
