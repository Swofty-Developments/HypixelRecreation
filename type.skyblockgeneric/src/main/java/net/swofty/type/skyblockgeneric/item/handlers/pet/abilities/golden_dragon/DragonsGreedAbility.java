package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golden_dragon;

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

@PetAbilityRegistration(pet = PetHandler.GOLDEN_DRAGON, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class DragonsGreedAbility implements PetAbility {
    private static final RarityValue<Double> PERCENT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.00125, 0.0, 0.0);
    private static final RarityValue<Double> MAX_PERCENT_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.025, 0.0, 0.0);

    @Override
    public String getName() {
        return "Dragon's Greed";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(0.125 + PERCENT_PER_LEVEL.getForRarity(rarity) * level, 2);
        String max = decimalify(2.5 + MAX_PERCENT_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <c>+" + percent + "% <stat:strength> <7>per",
                "<b>5 <stat:magic_find><7>. <8>(Max +" + max + "%)<7>"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double magicFind = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.MAGIC_FIND);
        if (magicFind < 5) return ItemStatistics.empty();

        double percent = (0.125 + PERCENT_PER_LEVEL.getForRarity(rarity) * level) * Math.floor(magicFind / 5);
        double max = 2.5 + MAX_PERCENT_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.STRENGTH, Math.min(percent, max))
                .build();
    }
}
