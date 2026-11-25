package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.enderman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ENDERMAN, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "Transmission abilities contains what?")
public final class TeleportSavvyAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.4, 0.5, 0.5, 0.5, 0.0);

    @Override
    public String getName() {
        return "Teleport Savvy";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Buffs the Transmission abilities, granting",
                "<a>" + decimalify(value, 1) + " <7>weapon damage for 5s on use"
        );
    }
}
