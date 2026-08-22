package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.mooshroom_cow;

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

@PetAbilityRegistration(pet = PetHandler.MOOSHROOM_COW, minimumRarity = Rarity.RARE)
public final class FarmingStrengthAbility implements PetAbility {
    private static final double FARMING_FORTUNE_PER_BRACKET = 0.7;
    private static final RarityValue<Double> STRENGTH_BASE =
            new RarityValue<>(0.0, 0.0, 40.0, 40.0, 40.0, 0.0, 0.0);
    private static final RarityValue<Double> STRENGTH_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, -0.2, -0.2, -0.2, 0.0, 0.0);

    @Override
    public String getName() {
        return "Farming Strength";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String threshold = decimalify(STRENGTH_BASE.getForRarity(rarity)
                + STRENGTH_PER_LEVEL.getForRarity(rarity) * level, 1);

        return List.of(
                "<7>Grants <6>+" + decimalify(FARMING_FORTUNE_PER_BRACKET, 1) + " <stat:farming_fortune> <7>for",
                "<7>every <c>" + threshold + " <stat:strength> <7>you have."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double threshold = STRENGTH_BASE.getForRarity(rarity) + STRENGTH_PER_LEVEL.getForRarity(rarity) * level;
        if (threshold <= 0) return ItemStatistics.empty();

        double strength = player.getStatistics().allNonPetStatistics(null, null)
                .getOverall(ItemStatistic.STRENGTH);
        double farmingFortune = Math.floor(strength / threshold) * FARMING_FORTUNE_PER_BRACKET;
        if (farmingFortune <= 0) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, farmingFortune)
                .build();
    }
}
