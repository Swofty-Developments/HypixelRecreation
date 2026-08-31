package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jellyfish;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.JELLYFISH, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits a Dungeons system + Power Orb mana-cost hook")
public final class RadiantScyphozoaAbility implements PetAbility {
    private static final RarityValue<Double> MANA_COST_REDUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.5, 0.5, 0.0, 0.0);

    @Override
    public String getName() {
        return "Radiant Scyphozoa";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(MANA_COST_REDUCTION_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>While in dungeons, reduces the mana cost",
                "<7>of Power Orbs by <a>" + percent + "%<7>."
        );
    }

    @PetEventHandler
    public void onManaCost(PetEvent.ManaCost event) {

    }
}
