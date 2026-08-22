package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.griffin;

import net.minestom.server.entity.attribute.Attribute;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GRIFFIN, minimumRarity = Rarity.RARE)
public final class SacredStrengthAbility implements PetAbility {
    private static final double HEALTH_THRESHOLD = 0.85;
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.15, 0.15, 0.15, 0.15, 0.0);

    @Override
    public String getName() {
        return "Sacred Strength";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String strength = decimalify(STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Gain <c>+" + strength + "% <stat:strength> <7>when",
                "<7>above <c>85% <7>health."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double maxHealth = player.getAttributeValue(Attribute.MAX_HEALTH);
        if (maxHealth <= 0 || player.getHealth() / maxHealth <= HEALTH_THRESHOLD) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.STRENGTH, STRENGTH_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
