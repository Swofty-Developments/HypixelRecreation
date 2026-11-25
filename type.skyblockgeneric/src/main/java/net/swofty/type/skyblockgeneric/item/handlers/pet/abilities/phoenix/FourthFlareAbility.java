package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.phoenix;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.commaify;
import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.PHOENIX, minimumRarity = Rarity.EPIC, order = 1,
        implemented = false, notImplementedReason = "awaits a 4th-strike ignite system")
public final class FourthFlareAbility implements PetAbility {
    private static final RarityValue<Double> MULTIPLIER_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0);
    private static final RarityValue<Double> MULTIPLIER_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.12, 0.14, 0.0, 0.0);
    private static final RarityValue<Double> DURATION_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 2.0, 2.0, 0.0, 0.0);
    private static final RarityValue<Double> DURATION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.02, 0.02, 0.0, 0.0);

    @Override
    public String getName() {
        return "Fourth Flare";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String multiplier = decimalify(MULTIPLIER_BASE.getForRarity(rarity)
                + MULTIPLIER_PER_LEVEL.getForRarity(rarity) * level, 2);
        String duration = commaify(Math.floor(DURATION_BASE.getForRarity(rarity)
                + DURATION_PER_LEVEL.getForRarity(rarity) * level));

        return List.of(
                "<7>On 4th melee strike, <6>ignite<7> mobs,",
                "<7>dealing <c>" + multiplier + "x <7>your <9><stat:crit_damage>",
                "<7>each second for <a>" + duration + " <7>seconds."
        );
    }

    @PetEventHandler
    public void onMeleeDamageDealt(PetEvent.MeleeDamageDealt event) {
    }
}
