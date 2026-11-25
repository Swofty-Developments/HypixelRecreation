package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ender_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

@PetAbilityRegistration(pet = PetHandler.ENDER_DRAGON, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "no Ender mobs right now")
public final class EndStrikeAbility implements PetAbility {
    private static final double PER_LEVEL = 2;

    @Override
    public String getName() {
        return "End Strike";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return Arrays.asList(
                "<7>Deal <a>" + commaify(value) + "% <7>more damage to <5>Ender",
                "<7>mobs."
        );
    }
}
