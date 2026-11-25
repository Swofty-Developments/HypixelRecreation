package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.owl;

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

@PetAbilityRegistration(pet = PetHandler.OWL, minimumRarity = Rarity.LEGENDARY, order = 2)
public final class FastLearnerAbility implements PetAbility {
    private static final RarityValue<Double> WISDOM_BASE =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.05, 0.0, 0.0);
    private static final RarityValue<Double> WISDOM_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.045, 0.0, 0.0);

    @Override
    public String getName() {
        return "Fast Learner";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(WISDOM_BASE.getForRarity(rarity)
                + WISDOM_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Passively grants <3>+" + value + " <stat:taming_wisdom>"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.TAMING_WISDOM, WISDOM_BASE.getForRarity(rarity) + WISDOM_PER_LEVEL.getForRarity(rarity) * level)
                .build();
    }
}
