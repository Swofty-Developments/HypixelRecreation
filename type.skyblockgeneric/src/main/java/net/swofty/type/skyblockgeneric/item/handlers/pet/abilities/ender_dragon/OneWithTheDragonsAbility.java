package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ender_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDER_DRAGON, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a held-item stat buff hook in getStatistics (buffing a held Aspect of the Dragons)")
public final class OneWithTheDragonsAbility implements PetAbility {
    private static final double DAMAGE_PER_LEVEL = 0.5;
    private static final double STRENGTH_PER_LEVEL = 0.3;

    @Override
    public String getName() {
        return "One with the Dragons";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double damage = DAMAGE_PER_LEVEL * level;
        double strength = STRENGTH_PER_LEVEL * level;

        return Arrays.asList(
                "<7>Buffs the <6>Aspect of the Dragons",
                "<7>sword by <a>" + decimalify(damage, 1) + " <stat:damage> <7>and <a>" + decimalify(strength, 1) + " ",
                "<stat:strength><7>."
        );
    }
}
