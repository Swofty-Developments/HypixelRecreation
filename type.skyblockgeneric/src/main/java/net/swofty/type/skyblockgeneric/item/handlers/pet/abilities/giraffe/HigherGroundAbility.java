package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.giraffe;

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

@PetAbilityRegistration(pet = PetHandler.GIRAFFE, minimumRarity = Rarity.RARE)
public final class HigherGroundAbility implements PetAbility {
    private static final double BASE_SWING_RANGE = 3;
    private static final double MAX_SWING_RANGE = 6;
    private static final RarityValue<Double> PERCENT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0015, 0.0015, 0.0015, 0.0, 0.0);

    @Override
    public String getName() {
        return "Higher Ground";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(PERCENT_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Increases your <9>Crit Damage <7>and",
                "<c>Strength <7>by <c>" + percent + "% <7>for every",
                "<e>0.1 <stat:swing_range> <7>over <e>" + BASE_SWING_RANGE + " <7>(up to <e>" + MAX_SWING_RANGE + "<7>)."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double swingRange = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.SWING_RANGE);
        double over = Math.min(swingRange, MAX_SWING_RANGE) - BASE_SWING_RANGE;
        if (over <= 0) return ItemStatistics.empty();

        double percent = PERCENT_PER_LEVEL.getForRarity(rarity) * level * over * 10;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.CRITICAL_DAMAGE, percent)
                .withAdditivePercentage(ItemStatistic.STRENGTH, percent)
                .build();
    }
}
