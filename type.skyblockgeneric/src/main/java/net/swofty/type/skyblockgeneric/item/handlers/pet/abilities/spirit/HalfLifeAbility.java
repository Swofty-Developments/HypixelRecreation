package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.spirit;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.SPIRIT, minimumRarity = Rarity.EPIC, order = 2,
        implemented = false, notImplementedReason = "awaits a dungeons score/death-penalty system")
public final class HalfLifeAbility implements PetAbility {

    @Override
    public String getName() {
        return "Half Life";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>If you are the first player to",
                "<7>die in a dungeon, the score",
                "<7>penalty for that death is",
                "<7>reduced to <a>1<7>."
        );
    }
}
