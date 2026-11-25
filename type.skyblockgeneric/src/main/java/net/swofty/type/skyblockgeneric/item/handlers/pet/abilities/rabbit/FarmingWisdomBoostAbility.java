package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.rabbit;

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

@PetAbilityRegistration(pet = PetHandler.RABBIT, minimumRarity = Rarity.RARE)
public final class FarmingWisdomBoostAbility implements PetAbility {
    private static final RarityValue<Double> WISDOM_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.25, 0.3, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Farming Wisdom Boost";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(WISDOM_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Grants <3>+" + value + " <stat:farming_wisdom><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_WISDOM, WISDOM_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
