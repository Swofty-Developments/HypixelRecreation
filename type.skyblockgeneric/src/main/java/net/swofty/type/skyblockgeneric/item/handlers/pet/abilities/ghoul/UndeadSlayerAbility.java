package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ghoul;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GHOUL, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits a unified XP-modifier pipeline; XP bonuses combine multiplicative (1.1x) + additive (+10%), so the kill handler can't just add XP")
public final class UndeadSlayerAbility implements PetAbility {
    private static final RarityValue<Double> XP_MULTIPLIER_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.005, 0.005, 0.0, 0.0);

    @Override
    public String getName() {
        return "Undead Slayer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String multiplier = decimalify(1 + XP_MULTIPLIER_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <b>" + multiplier + "x <7>Combat XP against",
                "<a>Zombies<7>."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob kill) {
    }
}
