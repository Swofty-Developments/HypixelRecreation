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

@PetAbilityRegistration(pet = PetHandler.PHOENIX, minimumRarity = Rarity.EPIC, order = 0,
        implemented = false, notImplementedReason = "awaits a death-prevention system (become immune before lethal damage); Damaged hook already dispatched")
public final class RekindleAbility implements PetAbility {
    private static final RarityValue<Double> STRENGTH_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 10.0, 15.0, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.1, 0.15, 0.0, 0.0);
    private static final RarityValue<Double> DURATION_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 2.0, 2.0, 0.0, 0.0);
    private static final RarityValue<Double> DURATION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.02, 0.02, 0.0, 0.0);

    @Override
    public String getName() {
        return "Rekindle";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String strength = decimalify(STRENGTH_BASE.getForRarity(rarity)
                + STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 1);
        String duration = commaify(Math.floor(DURATION_BASE.getForRarity(rarity)
                + DURATION_PER_LEVEL.getForRarity(rarity) * level));

        return List.of(
                "<7>Before death, become <e>immune",
                "<7>and gain <c>+" + strength + " <stat:strength> <7>for <a>" + duration,
                "<7>seconds <8>(1 minute cooldown)<7>."
        );
    }

    @PetEventHandler
    public void onDamaged(PetEvent.Damaged event) {
    }
}
