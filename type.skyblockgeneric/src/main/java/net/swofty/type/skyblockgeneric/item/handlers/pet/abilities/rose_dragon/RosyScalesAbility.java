package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rose_dragon;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.ROSE_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 1,
        implemented = false, notImplementedReason = "awaits a Crop Milestone system")
public final class RosyScalesAbility implements PetAbility {
    private static final double FORTUNE_BASE = 0.075;
    private static final double FORTUNE_PER_LEVEL = 0.00075;
    private static final double SPEED_BASE = 0.05;
    private static final double SPEED_PER_LEVEL = 0.0005;

    @Override
    public String getName() {
        return "Rosy Scales";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String fortune = decimalify(FORTUNE_BASE + FORTUNE_PER_LEVEL * level, 2);
        String speed = decimalify(SPEED_BASE + SPEED_PER_LEVEL * level, 2);

        return List.of(
                "<7>Grants <6>+" + fortune + " <stat:farming_fortune> <7>and",
                "<f>+" + speed + " <stat:speed><7> per Crop Milestone."
        );
    }
}
