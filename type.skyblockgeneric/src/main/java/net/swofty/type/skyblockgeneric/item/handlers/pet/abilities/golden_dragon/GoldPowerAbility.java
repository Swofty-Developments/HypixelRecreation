package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golden_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GOLDEN_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits Midas' Sword/Staff items + a Greed ability potency hook")
public final class GoldPowerAbility implements PetAbility {
    private static final RarityValue<Double> POTENCY_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.05, 0.0, 0.0);

    @Override
    public String getName() {
        return "Gold's Power";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String potency = decimalify(5 + POTENCY_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increase the potency of <6>Midas' Sword <7>and",
                "<6>Midas Staff's <5>Greed Ability <7>by <a>" + potency + "%<7>."
        );
    }

    @PetEventHandler
    public void onAbilityCast(PetEvent.AbilityCast event) {

    }
}
