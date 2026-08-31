package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.eerie;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.Arrays;
import java.util.List;

@PetAbilityRegistration(pet = PetHandler.EERIE, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a Fear value system (Great Spook Armor) + Primal Fears mobs")
public final class FearamaAbility implements PetAbility {
    @Override
    public String getName() {
        return "Fearama";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return Arrays.asList(
                "<7>Increases <c>damage <7>dealt to Primal",
                "<7>Fears and Spooky Mobs by <a>1% <7>for",
                "<7>every <5>Fear <7>you have."
        );
    }
}
