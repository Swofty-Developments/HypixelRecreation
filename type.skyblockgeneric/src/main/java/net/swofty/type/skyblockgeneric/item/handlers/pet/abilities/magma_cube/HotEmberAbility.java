package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.magma_cube;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.MAGMA_CUBE, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a per-item stat augmentation system for Rekindled Ember Armor")
public final class HotEmberAbility implements PetAbility {
    private static final RarityValue<Double> BUFF_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Hot Ember";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(BUFF_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Buffs the stats of <5>Rekindled Ember",
                "<5>Armor <7>by <a>" + percent + "%<7>."
        );
    }
}
