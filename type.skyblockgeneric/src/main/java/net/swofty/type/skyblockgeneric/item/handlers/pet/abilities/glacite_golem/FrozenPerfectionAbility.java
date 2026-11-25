package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.glacite_golem;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GLACITE_GOLEM, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Frozen Corpse looted-count system (per Glacite Mineshaft run)")
public final class FrozenPerfectionAbility implements PetAbility {
    private static final RarityValue<Double> PRISTINE_PER_CORPSE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.01, 0.0, 0.0);

    @Override
    public String getName() {
        return "Frozen Perfection";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(PRISTINE_PER_CORPSE.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <a>+" + value + " <stat:pristine> <7>for every",
                "<b>Frozen Corpse <7>you've looted in the",
                "<7>current <b>Glacite Mineshaft<7>."
        );
    }
}
