package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.precursor_drone;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.PRECURSOR_DRONE, minimumRarity = Rarity.COMMON, order = 1,
        implemented = false, notImplementedReason = "awaits a Foraging Axe throwing system")
public final class GrungleAbility implements PetAbility {
    @Override
    public String getName() {
        return "Grungle";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>You can now <c>ONLY <7>throw your",
                "<7>Foraging Axe, but it has <c>no",
                "<c>throwing penalty <7>anymore."
        );
    }
}
