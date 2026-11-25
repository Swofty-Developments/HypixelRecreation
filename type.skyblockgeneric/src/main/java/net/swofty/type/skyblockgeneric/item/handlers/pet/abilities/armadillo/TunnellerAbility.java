package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.armadillo;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.ARMADILLO, minimumRarity = Rarity.COMMON, order = 1,
        implemented = false, notImplementedReason = "Crystal Hollows region + tunneling system not implemented")
public final class TunnellerAbility implements PetAbility {
    @Override
    public String getName() {
        return "Tunneller";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>While in the <5>Crystal Hollows<7>, this Pet",
                "<7>breaks all blocks in its path using",
                "<7>your held item."
        );
    }
}
