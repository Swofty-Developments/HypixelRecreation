package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.orchid_mantis;

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

@PetAbilityRegistration(pet = PetHandler.ORCHID_MANTIS, minimumRarity = Rarity.RARE)
public final class SwiftSicklesAbility implements PetAbility {
    private static final double SPEED_PER_BRACKET = 3;
    private static final double SPEED_BASELINE = 100;
    private static final RarityValue<Double> FORTUNE_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.005, 0.01, 0.01, 0.0, 0.0);

    @Override
    public String getName() {
        return "Swift Sickles";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String value = decimalify(FORTUNE_PER_LEVEL.getForRarity(rarity) * level, 3);

        return List.of(
                "<7>Convert every <f>3 <stat:speed> <7>you have",
                "<7>above <f>100 <7>into <6>+" + value + " <stat:farming_fortune><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double speed = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.SPEED);
        double brackets = Math.floor((speed - SPEED_BASELINE) / SPEED_PER_BRACKET);
        if (brackets <= 0) return ItemStatistics.empty();

        double fortune = brackets * FORTUNE_PER_LEVEL.getForRarity(rarity) * level;
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, fortune)
                .build();
    }
}
