package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ocelot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.OCELOT, minimumRarity = Rarity.RARE,
        implemented = false, notImplementedReason = "awaits a MinionTick event + minion speed hook")
public final class TreeHuggerAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.3, 0.3, 0.3, 0.0, 0.0);

    @Override
    public String getName() {
        return "Tree Hugger";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(SPEED_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Foraging minions work <a>" + percent + "%",
                "<a><7>faster while on your island."
        );
    }
}
