package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spirit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.SPIRIT, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits a dungeons ghost system")
public final class SpiritAssistanceAbility implements PetAbility {

    @Override
    public String getName() {
        return "Spirit Assistance";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Spawns and assists you when",
                "<7>you are a ghost in Dungeons."
        );
    }
}
