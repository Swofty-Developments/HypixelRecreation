package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.griffin;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GRIFFIN, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "awaits a Griffin Burrow excavation system")
public final class AncientEarthAbility implements PetAbility {
    private static final int TRACKING_PER_BURROW = 1;

    @Override
    public String getName() {
        return "Ancient Earth";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>Grants <d>+" + TRACKING_PER_BURROW + " <stat:tracking> <7>on <e>Griffin",
                "<7>Burrows <7>for each burrow excavated in your",
                "<7>current chain."
        );
    }
}
