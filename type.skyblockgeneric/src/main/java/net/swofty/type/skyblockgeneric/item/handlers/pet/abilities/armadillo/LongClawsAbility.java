package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.armadillo;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ARMADILLO, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "no mining-context hook to gate 'while mining Hard Stone'; Mining Spread is not consumed by mining yet")
public final class LongClawsAbility implements PetAbility {
    private static final RarityValue<Double> MINING_SPREAD_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 3.0, 3.0, 0.0);

    @Override
    public String getName() {
        return "Long Claws";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = MINING_SPREAD_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Grants <e>" + decimalify(value, 1) + " <stat:mining_spread> <7>while",
                "<7>mining Hard Stone."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {

    }
}
