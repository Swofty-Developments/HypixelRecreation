package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snowman;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SNOWMAN, minimumRarity = Rarity.LEGENDARY, order = 0,
        implemented = false, notImplementedReason = "awaits a periodic nearby-entity aura system (slow + outgoing-damage reduction)")
public final class BlizzardAbility implements PetAbility {
    private static final double RADIUS_BASE = 8;
    private static final double RADIUS_PER_LEVEL = 0.08;
    private static final double DAMAGE_REDUCTION_PER_LEVEL = 0.1;

    @Override
    public String getName() {
        return "Blizzard";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String radius = decimalify(RADIUS_BASE + RADIUS_PER_LEVEL * level, 2);
        String reduction = decimalify(DAMAGE_REDUCTION_PER_LEVEL * level, 1);

        return List.of(
                "<7>Enemies within <a>" + radius + " <7>blocks are slowed",
                "<7>by <a>25% <7>and deal <a>" + reduction + "% <7>less damage."
        );
    }
}
