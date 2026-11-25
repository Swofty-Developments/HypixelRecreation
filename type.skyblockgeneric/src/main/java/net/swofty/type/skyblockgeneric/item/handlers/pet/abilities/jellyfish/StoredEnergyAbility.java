package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jellyfish;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.JELLYFISH, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a Dungeons system + teammate-heal tracking + Wish cooldown hook")
public final class StoredEnergyAbility implements PetAbility {
    private static final RarityValue<Double> COOLDOWN_REDUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.01, 0.01, 0.0, 0.0);

    @Override
    public String getName() {
        return "Stored Energy";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String seconds = decimalify(COOLDOWN_REDUCTION_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>While in dungeons, for every <c>2,000 HP <7>you",
                "<7>heal teammates the cooldown of <a>Wish <7>is",
                "<7>reduced by <a>" + seconds + "s<7>, up to <a>30s<7>."
        );
    }
}
