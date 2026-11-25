package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.eerie;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.EERIE, minimumRarity = Rarity.LEGENDARY,
        implemented = false, notImplementedReason = "awaits a Fear value system + Primal Fears mobs; Kill hook already dispatched")
public final class FearcreasingAbility implements PetAbility {
    private static final double BASE = 0.1;
    private static final double PER_LEVEL = 0.003;

    @Override
    public String getName() {
        return "Fearcreasing";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return Arrays.asList(
                "<7>Gives <a>+" + decimalify(value, 3) + " <5>Fear <7>for every <a>10 <c>Primal",
                "<c>Fears <7>killed, up to <a>150 <c>Primal Fears<7>.",
                "<c>Primal Fear Kills<7>: (<a>0<7>/<a>150<7>)"
        );
    }
}
