package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jellyfish;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.JELLYFISH, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a Dungeons system + Dungeon Potions effectiveness hook")
public final class PowerfulPotionsAbility implements PetAbility {
    private static final RarityValue<Double> EFFECTIVENESS_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Powerful Potions";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(EFFECTIVENESS_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>While in dungeons, increase the",
                "<7>effectiveness of Dungeon Potions by",
                "<a>" + percent + "%"
        );
    }
}
