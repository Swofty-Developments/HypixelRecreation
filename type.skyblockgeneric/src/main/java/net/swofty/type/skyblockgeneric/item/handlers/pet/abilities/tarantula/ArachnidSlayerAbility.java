package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.tarantula;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TARANTULA, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a unified XP-modifier pipeline")
public final class ArachnidSlayerAbility implements PetAbility {
    private static final double BASE = 1.0;
    private static final double PER_LEVEL = 0.005;

    @Override
    public String getName() {
        return "Arachnid Slayer";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = BASE + PER_LEVEL * level;

        return List.of(
                "<7>Gain <b>" + decimalify(value, 3) + "x <7>Combat XP <7>against",
                "<a>Spiders<7>."
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob event) {
    }
}
