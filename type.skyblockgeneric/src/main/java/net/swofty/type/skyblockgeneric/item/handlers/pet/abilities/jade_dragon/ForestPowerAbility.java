package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.jade_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.JADE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a per-item stat augmentation system for axes")
public final class ForestPowerAbility implements PetAbility {
    private static final double STRENGTH_BASE = 75;
    private static final double STRENGTH_PER_LEVEL = 0.25;
    private static final double SPEED_BASE = 37.5;
    private static final double SPEED_PER_LEVEL = 0.125;

    @Override
    public String getName() {
        return "Forest Power";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String strength = decimalify(STRENGTH_BASE + STRENGTH_PER_LEVEL * level, 1);
        String speed = decimalify(SPEED_BASE + SPEED_PER_LEVEL * level, 1);

        return List.of(
                "<7>Adds <c>+" + strength + " <stat:strength> <7>and <f>+" + speed,
                "<stat:speed> <7>to all your Axes."
        );
    }
}
