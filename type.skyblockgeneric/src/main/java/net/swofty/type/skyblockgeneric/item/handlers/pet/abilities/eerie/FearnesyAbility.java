package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.eerie;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.Arrays;
import java.util.List;

@PetAbilityRegistration(pet = PetHandler.EERIE, minimumRarity = Rarity.COMMON,
        implemented = false, notImplementedReason = "awaits a Fear value system (Great Spook Armor Fear applying from wardrobe)")
public final class FearnesyAbility implements PetAbility {
    @Override
    public String getName() {
        return "Fearnesy";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return Arrays.asList(
                "<5>Fear <7>from <5>Great Spook Armor <7>in",
                "<7>your <b>wardrobe <7>applies to you, even",
                "<7>if you aren't wearing it."
        );
    }
}
