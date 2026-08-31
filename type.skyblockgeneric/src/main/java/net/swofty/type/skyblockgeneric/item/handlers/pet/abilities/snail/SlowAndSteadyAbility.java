package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.snail;

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

@PetAbilityRegistration(pet = PetHandler.SNAIL, minimumRarity = Rarity.RARE)
public final class SlowAndSteadyAbility implements PetAbility {
    private static final RarityValue<Double> SPEED_NEEDED_BASE =
            new RarityValue<>(0.0, 0.0, 6.0, 6.0, 5.0, 0.0, 0.0);
    private static final RarityValue<Double> SPEED_NEEDED_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, -0.03, -0.03, -0.03, 0.0, 0.0);

    @Override
    public String getName() {
        return "Slow and Steady";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double needed = SPEED_NEEDED_BASE.getForRarity(rarity) + SPEED_NEEDED_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Convert every <f>" + decimalify(needed, 2) + " <stat:speed> <7>you have",
                "<7>above <f>100 <7>into <6>+1 <stat:block_fortune><7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        double needed = SPEED_NEEDED_BASE.getForRarity(rarity) + SPEED_NEEDED_PER_LEVEL.getForRarity(rarity) * level;
        double speed = player.getStatistics().allNonPetStatistics(null, null).getOverall(ItemStatistic.SPEED);
        double granted = speed > 100 ? Math.floor((speed - 100) / needed) : 0;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.BLOCK_FORTUNE, granted)
                .build();
    }
}
