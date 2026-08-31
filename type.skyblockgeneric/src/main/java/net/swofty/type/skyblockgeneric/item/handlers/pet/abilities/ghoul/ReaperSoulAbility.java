package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.ghoul;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GHOUL, minimumRarity = Rarity.LEGENDARY, order = 2,
        implemented = false, notImplementedReason = "awaits a mob summoning system (summon cost, damage output, health)")
public final class ReaperSoulAbility implements PetAbility {
    private static final RarityValue<Double> COST_REDUCTION_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0);
    private static final RarityValue<Double> DAMAGE_INCREASE_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.0);
    private static final RarityValue<Double> HEALTH_INCREASE_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    @Override
    public String getName() {
        return "Reaper Soul";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String cost = decimalify(COST_REDUCTION_PER_LEVEL.getForRarity(rarity) * level, 2);
        String damage = decimalify(DAMAGE_INCREASE_PER_LEVEL.getForRarity(rarity) * level, 2);
        String health = decimalify(HEALTH_INCREASE_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Reduces the summoning cost of mobs",
                "<7>by <a>" + cost + "% <7>and increases their damage",
                "<7>output by <a>" + damage + "%<7>. Increases the",
                "<7>health of all summoned mobs by <a>" + health + "%<7>."
        );
    }
}
