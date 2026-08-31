package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.hound;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HOUND, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a unified XP-modifier pipeline; XP bonuses combine multiplicative (1.1x) + additive (+10%), so the kill handler can't just add XP")
public final class PackSlayerAbility implements PetAbility {
    private static final double BASE = 1.0;
    private static final double PER_LEVEL = 0.005;

    @Override
    public String getName() {
        return "Pack Slayer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String multiplier = decimalify(BASE + PER_LEVEL * level, 3);

        return List.of(
                "<7>Gain <b>+" + multiplier + "x <7>Combat XP against <a>Wolves<7>."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob kill) {
    }
}
