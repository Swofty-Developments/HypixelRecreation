package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tarantula;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TARANTULA, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits a Tarantula Broodfather vitality-reduction mechanic")
public final class WebbedCellsAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.3, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Webbed Cells";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<c>Tarantula Broodfather's <4>Vitality<r>",
                "<7>reduction is <a>" + decimalify(value, 1) + "% <7>less effective",
                "<7>against you."
        );
    }

    @PetEventHandler
    public void onDamagedByMob(PetEvent.DamagedByMob event) {
    }
}
