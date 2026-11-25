package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.enderman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDERMAN, minimumRarity = Rarity.MYTHIC,
        implemented = false, notImplementedReason = "no mob type Enderman right now")
public final class EndermanSlayerAbility implements PetAbility {
    private static final double BASE = 1.0;
    private static final double PER_LEVEL = 0.005;

    @Override
    public String getName() {
        return "Enderman Slayer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return List.of(
                "<7>Gain <b>" + decimalify(value, 3) + "x <7>Combat XP against <a>Endermen<7>."
        );
    }
}
