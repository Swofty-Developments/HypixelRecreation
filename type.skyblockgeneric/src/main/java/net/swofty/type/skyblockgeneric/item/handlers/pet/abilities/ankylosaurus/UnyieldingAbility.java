package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ankylosaurus;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ANKYLOSAURUS, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "Last Stand/Lifeline + EnchantmentProc not implemented")
public final class UnyieldingAbility implements PetAbility {
    private static final double PER_LEVEL = 0.5;

    @Override
    public String getName() {
        return "Unyielding";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL * level;

        return List.of(
                "<7>Increase the effectiveness of <d><l>Last Stand<r> <7>and <6>Lifeline",
                "<7>by <a>" + decimalify(value, 1) + "%<7>."
        );
    }
}
