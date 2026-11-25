package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snail;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SNAIL, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "no mining-context hook to gate 'while mining Blocks'; Mining Spread is not consumed by mining yet")
public final class SlimyReachAbility implements PetAbility {
    private static final RarityValue<Double> MINING_SPREAD_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Slimy Reach";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String spread = decimalify(MINING_SPREAD_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <e>+" + spread + " <stat:mining_spread> <7>while",
                "<7>mining <9>Blocks<7>."
        );
    }

    @PetEventHandler
    public void onBlockMined(PetEvent.BlockMined event) {

    }
}
